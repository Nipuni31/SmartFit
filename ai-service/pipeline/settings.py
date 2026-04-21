"""
Central defaults for extraction and sizing. Override by passing ExtractionSettings
into extract / predict, or edit values here for your catalog.
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class ExtractionSettings:
    """OpenCV / geometry knobs (avoid magic numbers in extract + visualize)."""

    use_selfie_segmentation: bool = True
    selfie_model_asset_path: str = str(Path("models") / "selfie_multiclass_256x256.tflite")
    selfie_model_selection: int = 1
    segmentation_threshold: float = 0.50
    morph_kernel_size: int = 5
    morph_open_iterations: int = 1
    morph_close_iterations: int = 2
    grayscale_threshold: int = 127
    # Vertical slice positions as fraction of silhouette height (from top)
    shoulder_y_ratio: float = 0.20
    waist_y_ratio: float = 0.45
    hip_y_ratio: float = 0.55
    chest_y_ratio: float = 0.33  # side view: chest depth row
    # Extra horizontal profile samples (front view), fractions of height
    profile_y_ratios: tuple[float, ...] = (0.15, 0.25, 0.35, 0.45, 0.55, 0.65)


@dataclass(frozen=True)
class SizeBand:
    """Upper bounds (cm) for a size bucket."""

    label: str
    waist_max_cm: float
    hip_max_cm: float


@dataclass(frozen=True)
class TopSizeBand:
    """Tops: shoulder max + waist proxy max (cm)."""

    label: str
    shoulder_max_cm: float
    waist_max_cm: float


# Bottoms: waist + hip circumferences must fit within band maxima
BOTTOM_SIZE_BANDS: tuple[SizeBand, ...] = (
    SizeBand("XXS", 62.0, 86.0),
    SizeBand("XS", 66.0, 92.0),
    SizeBand("S", 72.0, 98.0),
    SizeBand("M", 78.0, 104.0),
    SizeBand("L", 84.0, 110.0),
    SizeBand("XL", 90.0, 116.0),
    SizeBand("XXL", 96.0, 124.0),
)

TOP_SIZE_BANDS: tuple[TopSizeBand, ...] = (
    TopSizeBand("XXS", 36.0, 64.0),
    TopSizeBand("XS", 38.0, 68.0),
    TopSizeBand("S", 40.0, 74.0),
    TopSizeBand("M", 42.0, 80.0),
    TopSizeBand("L", 44.0, 86.0),
    TopSizeBand("XL", 46.0, 92.0),
    TopSizeBand("XXL", 48.0, 98.0),
)

# Collapse detailed bands to retail letters for the ML heads
SIZE_LETTER_NORMALIZE: dict[str, str] = {
    "XXS": "S",
    "XS": "S",
    "S": "S",
    "M": "M",
    "L": "L",
    "XL": "XL",
    "XXL": "XL",
    "XXL+": "XL",
}

# Legacy utilization thresholds (avoid for labeling — they mark almost everyone "tight")
FIT_TIGHT_THRESHOLD: float = 0.88
FIT_LOOSE_THRESHOLD: float = 0.72

# Fit from "room" under the assigned band ceiling (fraction of headroom on the limiting dim).
# Small room → close to max of size → tight; large room → loose.
FIT_ROOM_TIGHT: float = 0.06
FIT_ROOM_LOOSE: float = 0.20

DEFAULT_EXTRACTION_SETTINGS = ExtractionSettings()
