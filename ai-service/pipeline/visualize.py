"""
Optional BGR overlays: contours, measurement lines, predicted size annotation.
"""

from __future__ import annotations

from pathlib import Path

import cv2
import numpy as np

from .extract import ExtractionResult, extract_full
from .settings import ExtractionSettings, DEFAULT_EXTRACTION_SETTINGS


def _to_bgr(gray_or_bgr: np.ndarray) -> np.ndarray:
    if gray_or_bgr.ndim == 2:
        return cv2.cvtColor(gray_or_bgr, cv2.COLOR_GRAY2BGR)
    return gray_or_bgr.copy()


def _read_image_source(image: str | Path | np.ndarray) -> np.ndarray:
    if isinstance(image, np.ndarray):
        arr = image
    else:
        arr = cv2.imread(str(image), cv2.IMREAD_UNCHANGED)
        if arr is None:
            raise FileNotFoundError(f"Could not read image: {image}")
    if arr.ndim == 2:
        return cv2.cvtColor(arr, cv2.COLOR_GRAY2BGR)
    if arr.ndim == 3 and arr.shape[2] == 4:
        return cv2.cvtColor(arr, cv2.COLOR_BGRA2BGR)
    return arr.copy()


def render_front_overlay(
    extraction: ExtractionResult,
    *,
    title: str = "",
    line_thickness: int = 2,
) -> np.ndarray:
    """Draw largest contour + shoulder / waist / hip spans on the front silhouette."""
    g = extraction.geometry
    vis = _to_bgr(g.front_bin)
    if g.contour_front.size > 0:
        cv2.drawContours(vis, [g.contour_front], -1, (0, 255, 255), 1)

    sx1, sx2 = g.shoulder_span
    wx1, wx2 = g.waist_span_front
    hx1, hx2 = g.hip_span_front

    cv2.line(vis, (sx1, g.shoulder_y), (sx2, g.shoulder_y), (255, 80, 80), line_thickness)
    cv2.line(vis, (wx1, g.waist_y), (wx2, g.waist_y), (80, 255, 80), line_thickness)
    cv2.line(vis, (hx1, g.hip_y), (hx2, g.hip_y), (80, 80, 255), line_thickness)

    y0 = max(24, g.top + 10)
    cv2.putText(
        vis,
        title,
        (10, y0),
        cv2.FONT_HERSHEY_SIMPLEX,
        0.55,
        (240, 240, 240),
        2,
        cv2.LINE_AA,
    )
    return vis


def render_side_overlay(
    extraction: ExtractionResult,
    *,
    line_thickness: int = 2,
) -> np.ndarray:
    g = extraction.geometry
    vis = _to_bgr(g.side_bin)
    if g.contour_side.size > 0:
        cv2.drawContours(vis, [g.contour_side], -1, (0, 255, 255), 1)

    cx1, cx2 = g.chest_span_side
    wx1, wx2 = g.waist_span_side
    hx1, hx2 = g.hip_span_side

    cv2.line(vis, (cx1, g.chest_y_side), (cx2, g.chest_y_side), (200, 200, 80), line_thickness)
    cv2.line(vis, (wx1, g.waist_y_side), (wx2, g.waist_y_side), (80, 255, 80), line_thickness)
    cv2.line(vis, (hx1, g.hip_y_side), (hx2, g.hip_y_side), (80, 80, 255), line_thickness)
    return vis


def build_visualization_pair(
    front_img: str | Path | np.ndarray,
    side_img: str | Path | np.ndarray,
    *,
    height_cm: float,
    prediction_summary: str,
    settings: ExtractionSettings | None = None,
) -> tuple[np.ndarray, np.ndarray]:
    """
    Convenience: extract + render both views.

    `prediction_summary` is shown on the front panel (e.g. "Top M | Bottom L | normal").
    """
    cfg = settings or DEFAULT_EXTRACTION_SETTINGS
    ex = extract_full(front_img, side_img, height_cm=height_cm, settings=cfg)
    front_vis = render_front_overlay(ex, title=prediction_summary)
    side_vis = render_side_overlay(ex)
    return front_vis, side_vis


def save_visualization_pair(
    front_vis: np.ndarray,
    side_vis: np.ndarray,
    front_out: str | Path,
    side_out: str | Path,
) -> None:
    cv2.imwrite(str(front_out), front_vis)
    cv2.imwrite(str(side_out), side_vis)


def build_debug_visualization_pair(
    front_img: str | Path | np.ndarray,
    side_img: str | Path | np.ndarray,
    *,
    height_cm: float,
    prediction_summary: str = "",
    settings: ExtractionSettings | None = None,
) -> dict[str, np.ndarray]:
    """
    Optional debugging views for each stage:
    - original_front / original_side (raw uploads)
    - mask_front / mask_side (segmentation + morphology output)
    - overlay_front / overlay_side (contour + measurement lines)
    """
    cfg = settings or DEFAULT_EXTRACTION_SETTINGS
    ex = extract_full(front_img, side_img, height_cm=height_cm, settings=cfg)
    return {
        "original_front": _read_image_source(front_img),
        "original_side": _read_image_source(side_img),
        "mask_front": ex.geometry.front_bin.copy(),
        "mask_side": ex.geometry.side_bin.copy(),
        "overlay_front": render_front_overlay(ex, title=prediction_summary),
        "overlay_side": render_side_overlay(ex),
    }
