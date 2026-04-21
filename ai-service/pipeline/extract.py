"""
Image -> metric measurements (OpenCV + MediaPipe segmentation).

Pipeline: RGB/BGR image -> MediaPipe person mask -> morphology cleanup ->
largest contour mask -> row-wise torso width -> ellipse waist/hip
circumferences; side view depths for chest and hip.
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
from typing import Any

import cv2
import numpy as np

from .settings import DEFAULT_EXTRACTION_SETTINGS, ExtractionSettings

try:
    import mediapipe as mp
except Exception:  # pragma: no cover - runtime dependency availability
    mp = None

try:
    from mediapipe.tasks.python.core.base_options import BaseOptions
    from mediapipe.tasks.python.vision import ImageSegmenter, ImageSegmenterOptions, RunningMode
except Exception:  # pragma: no cover - runtime dependency availability
    BaseOptions = None
    ImageSegmenter = None
    ImageSegmenterOptions = None
    RunningMode = None

# Type alias for public API dict (JSON-serializable floats)
MeasurementsDict = dict[str, float]


@dataclass(frozen=True)
class GeometryOverlay:
    """Everything `visualize.py` needs without re-parsing geometry."""

    front_bin: np.ndarray
    side_bin: np.ndarray
    contour_front: np.ndarray
    contour_side: np.ndarray
    top: int
    bottom: int
    height_px: int
    shoulder_y: int
    waist_y: int
    hip_y: int
    chest_y_front: int
    chest_y_side: int
    waist_y_side: int
    hip_y_side: int
    shoulder_span: tuple[int, int]
    waist_span_front: tuple[int, int]
    hip_span_front: tuple[int, int]
    chest_span_side: tuple[int, int]
    waist_span_side: tuple[int, int]
    hip_span_side: tuple[int, int]


@dataclass(frozen=True)
class ExtractionResult:
    """Structured output: public measurements + ML feature base + viz geometry."""

    measurements: MeasurementsDict
    feature_base: dict[str, float]
    geometry: GeometryOverlay
    pixel_to_cm: float


def _to_array(image: str | Path | np.ndarray) -> np.ndarray:
    if isinstance(image, np.ndarray):
        if image.ndim == 3:
            return cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        return image
    img = cv2.imread(str(image), cv2.IMREAD_GRAYSCALE)
    if img is None:
        raise FileNotFoundError(f"Could not read image: {image}")
    if img.ndim == 3:
        return cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    return img


def _to_bgr(image: str | Path | np.ndarray) -> np.ndarray:
    if isinstance(image, np.ndarray):
        if image.ndim == 2:
            return cv2.cvtColor(image, cv2.COLOR_GRAY2BGR)
        if image.ndim == 3 and image.shape[2] == 4:
            return cv2.cvtColor(image, cv2.COLOR_BGRA2BGR)
        return image

    img = cv2.imread(str(image), cv2.IMREAD_COLOR)
    if img is None:
        raise FileNotFoundError(f"Could not read image: {image}")
    return img


def _clean_binary_mask(binary_mask: np.ndarray, settings: ExtractionSettings) -> np.ndarray:
    mask = binary_mask.copy()
    k = max(1, int(settings.morph_kernel_size))
    if k % 2 == 0:
        k += 1
    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (k, k))

    if settings.morph_open_iterations > 0:
        mask = cv2.morphologyEx(
            mask,
            cv2.MORPH_OPEN,
            kernel,
            iterations=settings.morph_open_iterations,
        )
    if settings.morph_close_iterations > 0:
        mask = cv2.morphologyEx(
            mask,
            cv2.MORPH_CLOSE,
            kernel,
            iterations=settings.morph_close_iterations,
        )
    return mask


def _segment_person_binary(
    bgr_image: np.ndarray,
    segmenter: Any,
    settings: ExtractionSettings,
) -> np.ndarray:
    rgb = cv2.cvtColor(bgr_image, cv2.COLOR_BGR2RGB)
    result = segmenter.process(rgb)
    if result.segmentation_mask is None:
        raise ValueError("MediaPipe segmentation returned an empty mask.")
    mask = (result.segmentation_mask >= settings.segmentation_threshold).astype(np.uint8) * 255
    return _clean_binary_mask(mask, settings)


def _resolve_selfie_model_asset_path(settings: ExtractionSettings) -> str:
    p = Path(settings.selfie_model_asset_path)
    if not p.is_absolute():
        p = Path(__file__).resolve().parent.parent / p
    if not p.is_file():
        raise FileNotFoundError(
            "MediaPipe Tasks model not found. Download `selfie_multiclass_256x256.tflite` "
            f"and place it at: {p}"
        )
    return str(p)


def _segment_person_binary_tasks(
    bgr_image: np.ndarray,
    segmenter: Any,
    settings: ExtractionSettings,
) -> np.ndarray:
    if mp is None:
        raise ImportError("mediapipe is not available.")
    rgb = cv2.cvtColor(bgr_image, cv2.COLOR_BGR2RGB)
    mp_image = mp.Image(image_format=mp.ImageFormat.SRGB, data=rgb)
    result = segmenter.segment(mp_image)

    if getattr(result, "confidence_masks", None):
        background_conf = result.confidence_masks[0].numpy_view()
        foreground_conf = 1.0 - background_conf
        mask = (foreground_conf >= settings.segmentation_threshold).astype(np.uint8) * 255
    elif getattr(result, "category_mask", None) is not None:
        category_mask = result.category_mask.numpy_view()
        mask = (category_mask > 0).astype(np.uint8) * 255
    else:
        raise ValueError("MediaPipe Tasks segmentation returned no usable mask output.")

    return _clean_binary_mask(mask, settings)


def _preprocess_binary(
    image: str | Path | np.ndarray,
    settings: ExtractionSettings,
    segmenter: Any | None = None,
    segmenter_mode: str = "solutions",
) -> np.ndarray:
    if settings.use_selfie_segmentation:
        if segmenter is None:
            raise ValueError("Expected a MediaPipe segmenter when segmentation is enabled.")
        bgr = _to_bgr(image)
        if segmenter_mode == "tasks":
            return _segment_person_binary_tasks(bgr, segmenter, settings)
        return _segment_person_binary(bgr, segmenter, settings)

    gray = _to_array(image)
    _, binary = cv2.threshold(gray, settings.grayscale_threshold, 255, cv2.THRESH_BINARY)
    return _clean_binary_mask(binary, settings)


def _largest_contour_mask(binary: np.ndarray) -> tuple[np.ndarray, np.ndarray]:
    contours, _ = cv2.findContours(binary, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    if not contours:
        return binary, np.empty((0, 1, 2), dtype=np.int32)
    cnt = max(contours, key=cv2.contourArea)
    mask = np.zeros_like(binary)
    cv2.drawContours(mask, [cnt], -1, 255, thickness=-1)
    return mask, cnt


def _get_torso_width(mask: np.ndarray, y: int) -> tuple[int, int, int]:
    """Return (x1, x2, width_px) for the foreground run closest to horizontal center."""
    row = mask[y]
    segments: list[tuple[int, int]] = []
    in_seg = False
    start = 0
    for i in range(len(row)):
        if row[i] >= 255 and not in_seg:
            start = i
            in_seg = True
        elif row[i] < 255 and in_seg:
            segments.append((start, i))
            in_seg = False
    if in_seg:
        segments.append((start, len(row)))
    if not segments:
        return 0, 0, 0
    center = len(row) // 2
    best = min(segments, key=lambda seg: abs((seg[0] + seg[1]) // 2 - center))
    x1, x2 = best
    return x1, x2, x2 - x1


def _ellipse_circumference(a: float, b: float) -> float:
    return float(np.pi * (3 * (a + b) - np.sqrt((3 * a + b) * (a + 3 * b))))


def extract_measurements(
    front_img: str | Path | np.ndarray,
    side_img: str | Path | np.ndarray,
    *,
    height_cm: float,
    settings: ExtractionSettings | None = None,
) -> MeasurementsDict:
    """
    Public API: return only the six measurement fields (cm).

    - shoulder: front shoulder breadth
    - waist / hip: ellipse circumferences from front width + side depth
    - height: calibration height (same as input `height_cm`)
    - chest_depth / hip_depth: side-view depths at chest and hip rows
    """
    result = extract_full(front_img, side_img, height_cm=height_cm, settings=settings)
    return dict(result.measurements)


def extract_full(
    front_img: str | Path | np.ndarray,
    side_img: str | Path | np.ndarray,
    *,
    height_cm: float,
    settings: ExtractionSettings | None = None,
) -> ExtractionResult:
    """
    Full extraction for features + visualization.

    `height_cm` scales pixels → centimeters (known or estimated full body height).
    """
    cfg = settings or DEFAULT_EXTRACTION_SETTINGS

    if cfg.use_selfie_segmentation:
        if mp is None:
            raise ImportError("MediaPipe is required for selfie segmentation. Install with: pip install mediapipe")

        has_solutions = hasattr(mp, "solutions") and hasattr(
            getattr(mp, "solutions"), "selfie_segmentation"
        )
        has_tasks = all(
            v is not None for v in (BaseOptions, ImageSegmenter, ImageSegmenterOptions, RunningMode)
        )

        if has_solutions:
            with mp.solutions.selfie_segmentation.SelfieSegmentation(
                model_selection=cfg.selfie_model_selection
            ) as segmenter:
                front_bin = _preprocess_binary(front_img, cfg, segmenter=segmenter, segmenter_mode="solutions")
                side_bin = _preprocess_binary(side_img, cfg, segmenter=segmenter, segmenter_mode="solutions")
        elif has_tasks:
            model_path = _resolve_selfie_model_asset_path(cfg)
            options = ImageSegmenterOptions(
                base_options=BaseOptions(model_asset_path=model_path),
                running_mode=RunningMode.IMAGE,
                output_confidence_masks=True,
                output_category_mask=True,
            )
            try:
                with ImageSegmenter.create_from_options(options) as segmenter:
                    front_bin = _preprocess_binary(front_img, cfg, segmenter=segmenter, segmenter_mode="tasks")
                    side_bin = _preprocess_binary(side_img, cfg, segmenter=segmenter, segmenter_mode="tasks")
            except Exception as exc:
                raise RuntimeError(
                    "Failed to initialize MediaPipe Tasks segmenter. Ensure "
                    "`selfie_model_asset_path` points to a valid Image Segmenter model file."
                ) from exc
        else:
            raise ImportError(
                "Neither MediaPipe `solutions` nor `tasks` APIs are available for segmentation."
            )
    else:
        front_bin = _preprocess_binary(front_img, cfg)
        side_bin = _preprocess_binary(side_img, cfg)

    front_mask, cnt_f = _largest_contour_mask(front_bin)
    side_mask, cnt_s = _largest_contour_mask(side_bin)

    ys, _ = np.where(front_mask >= 255)
    if ys.size == 0:
        raise ValueError("Front silhouette is empty after contour extraction.")
    top = int(np.min(ys))
    bottom = int(np.max(ys))
    height_px = bottom - top
    if height_px <= 0:
        raise ValueError("Invalid silhouette height in pixels.")

    def _y_at(ratio: float) -> int:
        return int(np.clip(top + ratio * height_px, top, bottom))

    shoulder_y = _y_at(cfg.shoulder_y_ratio)
    waist_y = _y_at(cfg.waist_y_ratio)
    hip_y = _y_at(cfg.hip_y_ratio)
    chest_y_front = _y_at(cfg.chest_y_ratio)

    def map_to_side(y_front: int) -> int:
        ratio = (y_front - top) / height_px
        return int(np.clip(ratio * side_mask.shape[0], 0, side_mask.shape[0] - 1))

    chest_y_side = map_to_side(chest_y_front)
    waist_y_side = map_to_side(waist_y)
    hip_y_side = map_to_side(hip_y)

    s_x1, s_x2, s_w = _get_torso_width(front_mask, shoulder_y)
    w_x1, w_x2, w_w = _get_torso_width(front_mask, waist_y)
    h_x1, h_x2, h_w = _get_torso_width(front_mask, hip_y)

    _, _, chest_d = _get_torso_width(side_mask, chest_y_side)
    _, _, waist_d = _get_torso_width(side_mask, waist_y_side)
    _, _, hip_d = _get_torso_width(side_mask, hip_y_side)

    pixel_to_cm = float(height_cm) / float(height_px)

    shoulder_cm = s_w * pixel_to_cm
    waist_front_cm = w_w * pixel_to_cm
    waist_depth_cm = waist_d * pixel_to_cm
    hip_front_cm = h_w * pixel_to_cm
    hip_depth_cm = hip_d * pixel_to_cm
    chest_depth_cm = chest_d * pixel_to_cm

    waist_circ = _ellipse_circumference(waist_front_cm / 2, waist_depth_cm / 2)
    hip_circ = _ellipse_circumference(hip_front_cm / 2, hip_depth_cm / 2)

    slice_widths: list[float] = []
    for r in cfg.profile_y_ratios:
        yy = int(top + r * height_px)
        yy = int(np.clip(yy, top, bottom))
        _, _, ww = _get_torso_width(front_mask, yy)
        slice_widths.append(float(ww))
    max_w = max(slice_widths) if slice_widths else 1.0
    profile_norm = [w / max_w for w in slice_widths]

    torso_px = max(1, waist_y - top)
    lower_px = max(1, bottom - waist_y)
    torso_leg_ratio = lower_px / torso_px

    measurements: MeasurementsDict = {
        "shoulder": float(shoulder_cm),
        "waist": float(waist_circ),
        "hip": float(hip_circ),
        "height": float(height_cm),
        "chest_depth": float(chest_depth_cm),
        "hip_depth": float(hip_depth_cm),
    }

    feature_base: dict[str, float] = {
        "shoulder_cm": float(shoulder_cm),
        "waist_circ_cm": float(waist_circ),
        "hip_circ_cm": float(hip_circ),
        "height_cm": float(height_cm),
        "chest_depth_cm": float(chest_depth_cm),
        "hip_depth_cm": float(hip_depth_cm),
        "waist_front_cm": float(waist_front_cm),
        "waist_depth_cm": float(waist_depth_cm),
        "hip_front_cm": float(hip_front_cm),
        "torso_leg_ratio": float(torso_leg_ratio),
    }
    for i, v in enumerate(profile_norm):
        feature_base[f"profile_norm_{i}"] = float(v)

    cx1, cx2, _ = _get_torso_width(side_mask, chest_y_side)
    wx1s, wx2s, _ = _get_torso_width(side_mask, waist_y_side)
    hx1s, hx2s, _ = _get_torso_width(side_mask, hip_y_side)

    geom = GeometryOverlay(
        front_bin=front_mask,
        side_bin=side_mask,
        contour_front=cnt_f,
        contour_side=cnt_s,
        top=top,
        bottom=bottom,
        height_px=height_px,
        shoulder_y=shoulder_y,
        waist_y=waist_y,
        hip_y=hip_y,
        chest_y_front=chest_y_front,
        chest_y_side=chest_y_side,
        waist_y_side=waist_y_side,
        hip_y_side=hip_y_side,
        shoulder_span=(s_x1, s_x2),
        waist_span_front=(w_x1, w_x2),
        hip_span_front=(h_x1, h_x2),
        chest_span_side=(cx1, cx2),
        waist_span_side=(wx1s, wx2s),
        hip_span_side=(hx1s, hx2s),
    )

    return ExtractionResult(
        measurements=measurements,
        feature_base=feature_base,
        geometry=geom,
        pixel_to_cm=pixel_to_cm,
    )


def extraction_result_to_jsonable(result: ExtractionResult) -> dict[str, Any]:
    """Drop numpy arrays for logging / APIs."""
    return {
        "measurements": result.measurements,
        "feature_base": result.feature_base,
        "pixel_to_cm": result.pixel_to_cm,
    }
