"""
Measurements + silhouette profile → fixed-order feature vector for ML.

`FEATURE_ORDER` must stay stable across training, `model.save`, and inference.
"""

from __future__ import annotations

from typing import Any

import numpy as np
import pandas as pd

from .extract import ExtractionResult
from .settings import DEFAULT_EXTRACTION_SETTINGS, ExtractionSettings


def _ellipse_circumference_semi(a: float, b: float) -> float:
    return float(np.pi * (3 * (a + b) - np.sqrt((3 * a + b) * (a + 3 * b))))


def _semi_axes_from_circumference(circ_cm: float, depth_over_front: float) -> tuple[float, float]:
    """
    Given target ellipse circumference and semi-axis ratio b/a (depth / half-width),
    solve for semi-major a (front half-width) and b (depth half) with b = (b/a)*a.
    """
    if circ_cm <= 0:
        return 0.0, 0.0
    r = max(1e-6, depth_over_front)

    def circ_from_a(a: float) -> float:
        b = r * a
        return _ellipse_circumference_semi(a, b)

    lo, hi = 1e-4, max(circ_cm, 1e-3)
    for _ in range(60):
        mid = 0.5 * (lo + hi)
        if circ_from_a(mid) < circ_cm:
            lo = mid
        else:
            hi = mid
    a = 0.5 * (lo + hi)
    b = r * a
    return float(a), float(b)


def _profile_len(settings: ExtractionSettings) -> int:
    return len(settings.profile_y_ratios)


def _profile_keys(settings: ExtractionSettings) -> tuple[str, ...]:
    n = _profile_len(settings)
    return tuple(f"profile_norm_{i}" for i in range(n))


def build_feature_order(settings: ExtractionSettings | None = None) -> tuple[str, ...]:
    """Dynamic order: base keys + one entry per vertical profile sample."""
    cfg = settings or DEFAULT_EXTRACTION_SETTINGS
    base = (
        "height_cm",
        "shoulder_cm",
        "waist_circ_cm",
        "hip_circ_cm",
        "chest_depth_cm",
        "hip_depth_cm",
        "waist_front_cm",
        "waist_depth_cm",
        "hip_front_cm",
        "torso_leg_ratio",
        "waist_hip_ratio",
        "shoulder_hip_ratio",
        "shoulder_waist_ratio",
        "chest_hip_depth_ratio",
        "waist_depth_to_height_ratio",
        "hip_depth_to_height_ratio",
        "mean_depth_to_height_ratio",
    )
    return base + _profile_keys(cfg)


# Default training / inference order (uses default profile slice count)
FEATURE_ORDER: tuple[str, ...] = build_feature_order(DEFAULT_EXTRACTION_SETTINGS)


def _ratio(num: float, den: float) -> float:
    return float(num / den) if den > 0 else 0.0


def estimate_profile_norms_from_body_row(
    row: pd.Series,
    *,
    settings: ExtractionSettings | None = None,
    waist_depth_over_front: float = 0.82,
    hip_depth_over_front: float = 0.78,
) -> list[float]:
    """
    Approximate vertical width profile using only tabular fields (training without masks).

    Must NOT be all 1.0 — that made CSV-trained models ignore profile_* at train time while
    inference varied them, hurting generalization.
    """
    cfg = settings or DEFAULT_EXTRACTION_SETTINGS
    s = float(row["shoulder-breadth"])
    w_circ = float(row["waist"])
    hip_circ = float(row["hip"])

    a_w, _ = _semi_axes_from_circumference(w_circ, waist_depth_over_front)
    a_h, _ = _semi_axes_from_circumference(hip_circ, hip_depth_over_front)
    waist_front = 2.0 * a_w
    hip_front = 2.0 * a_h

    widths: list[float] = []
    for t in cfg.profile_y_ratios:
        # Top slices: shoulder-dominated; lower slices: blend toward waist/hip front widths.
        shoulder_term = (1.0 - t) * s
        mid_term = t * waist_front
        hip_term = 0.35 * t * hip_front
        w_est = max(shoulder_term + mid_term + hip_term, 1e-6)
        widths.append(float(w_est))

    mx = max(widths) if widths else 1.0
    return [float(w / mx) for w in widths]


def build_features_from_extraction(
    result: ExtractionResult,
    settings: ExtractionSettings | None = None,
) -> dict[str, float]:
    """Features from a live `extract_full` result (matches inference)."""
    cfg = settings or DEFAULT_EXTRACTION_SETTINGS
    m = result.measurements
    fb = result.feature_base
    w = m["waist"]
    hip = m["hip"]
    s = m["shoulder"]
    h_cm = m["height"]
    cd = m["chest_depth"]
    hd = m["hip_depth"]

    keys = _profile_keys(cfg)
    profile_vals = [float(fb.get(k, 0.0)) for k in keys]
    if len(profile_vals) != len(keys):
        raise ValueError("feature_base profile keys mismatch; rebuild with matching ExtractionSettings.")

    feats: dict[str, float] = {
        "height_cm": float(h_cm),
        "shoulder_cm": float(s),
        "waist_circ_cm": float(w),
        "hip_circ_cm": float(hip),
        "chest_depth_cm": float(cd),
        "hip_depth_cm": float(hd),
        "waist_front_cm": float(fb["waist_front_cm"]),
        "waist_depth_cm": float(fb["waist_depth_cm"]),
        "hip_front_cm": float(fb["hip_front_cm"]),
        "torso_leg_ratio": float(fb["torso_leg_ratio"]),
        "waist_hip_ratio": _ratio(w, hip),
        "shoulder_hip_ratio": _ratio(s, hip),
        "shoulder_waist_ratio": _ratio(s, w),
        "chest_hip_depth_ratio": _ratio(cd, hd),
        "waist_depth_to_height_ratio": _ratio(fb["waist_depth_cm"], h_cm),
        "hip_depth_to_height_ratio": _ratio(hd, h_cm),
        "mean_depth_to_height_ratio": _ratio(0.5 * (cd + hd), h_cm),
    }
    for k, v in zip(keys, profile_vals, strict=True):
        feats[k] = v
    return feats


def estimate_feature_dict_from_body_row(
    row: pd.Series,
    *,
    settings: ExtractionSettings | None = None,
    waist_depth_over_front: float = 0.82,
    hip_depth_over_front: float = 0.78,
) -> dict[str, float]:
    """
    Build the same feature dict from a tabular row (training without silhouettes).

    Expects columns: height, shoulder-breadth, waist, hip, and optionally leg-length.
    Depths / front splits are inferred from circumferences with stable depth/width priors.
    """
    cfg = settings or DEFAULT_EXTRACTION_SETTINGS
    h_cm = float(row["height"])
    s = float(row["shoulder-breadth"])
    w_circ = float(row["waist"])
    hip_circ = float(row["hip"])
    leg = float(row["leg-length"]) if "leg-length" in row and pd.notna(row["leg-length"]) else h_cm * 0.48
    torso = max(1.0, h_cm - leg)
    torso_leg_ratio = leg / torso

    a_w, b_w = _semi_axes_from_circumference(w_circ, waist_depth_over_front)
    waist_front_cm = 2.0 * a_w
    waist_depth_cm = 2.0 * b_w

    a_h, b_h = _semi_axes_from_circumference(hip_circ, hip_depth_over_front)
    hip_front_cm = 2.0 * a_h
    hip_depth_cm = 2.0 * b_h

    chest_depth_cm = float(np.clip(0.22 * h_cm, 5.0, 60.0))

    n = _profile_len(cfg)
    profile_list = estimate_profile_norms_from_body_row(
        row,
        settings=cfg,
        waist_depth_over_front=waist_depth_over_front,
        hip_depth_over_front=hip_depth_over_front,
    )
    if len(profile_list) != n:
        raise RuntimeError("profile_y_ratios length mismatch")

    feats: dict[str, float] = {
        "height_cm": h_cm,
        "shoulder_cm": s,
        "waist_circ_cm": w_circ,
        "hip_circ_cm": hip_circ,
        "chest_depth_cm": chest_depth_cm,
        "hip_depth_cm": hip_depth_cm,
        "waist_front_cm": waist_front_cm,
        "waist_depth_cm": waist_depth_cm,
        "hip_front_cm": hip_front_cm,
        "torso_leg_ratio": torso_leg_ratio,
        "waist_hip_ratio": _ratio(w_circ, hip_circ),
        "shoulder_hip_ratio": _ratio(s, hip_circ),
        "shoulder_waist_ratio": _ratio(s, w_circ),
        "chest_hip_depth_ratio": _ratio(chest_depth_cm, hip_depth_cm),
        "waist_depth_to_height_ratio": _ratio(waist_depth_cm, h_cm),
        "hip_depth_to_height_ratio": _ratio(hip_depth_cm, h_cm),
        "mean_depth_to_height_ratio": _ratio(0.5 * (chest_depth_cm + hip_depth_cm), h_cm),
    }
    for i, v in enumerate(profile_list):
        feats[f"profile_norm_{i}"] = float(v)
    return feats


def features_dict_to_vector(
    features: dict[str, float],
    feature_order: tuple[str, ...] | None = None,
) -> np.ndarray:
    order = feature_order or FEATURE_ORDER
    return np.array([float(features[k]) for k in order], dtype=np.float64)


def vector_to_feature_dict(vec: np.ndarray, feature_order: tuple[str, ...] | None = None) -> dict[str, float]:
    order = feature_order or FEATURE_ORDER
    return {k: float(v) for k, v in zip(order, vec, strict=True)}


def bundle_feature_order(bundle: dict[str, Any]) -> tuple[str, ...]:
    fo = bundle.get("feature_order")
    if fo is None:
        return FEATURE_ORDER
    return tuple(fo)


def describe_feature_matrix(X: np.ndarray, feature_order: tuple[str, ...]) -> None:
    """Debug: variance per feature (detect dead columns)."""
    std = np.std(X, axis=0)
    print("Feature std (min / max):", float(std.min()), "/", float(std.max()))
    dead = [feature_order[i] for i in range(len(std)) if std[i] < 1e-9]
    if dead:
        print("WARNING: near-constant features:", dead[:12], ("..." if len(dead) > 12 else ""))
