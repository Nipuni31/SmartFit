"""
Prediction API: images → measurements → features → multi-head size model.
"""

from __future__ import annotations

from pathlib import Path
from typing import Any, Union

import numpy as np

from .extract import extract_full
from .features import build_features_from_extraction, features_dict_to_vector
from .model import SizeModelBundle, decode_predictions, load_bundle, per_head_max_proba
from .settings import ExtractionSettings

ImageSource = Union[str, Path, np.ndarray]
ModelInput = Union[str, Path, SizeModelBundle]


def predict_size(
    front_img: ImageSource,
    side_img: ImageSource,
    model: ModelInput,
    *,
    height_cm: float,
    settings: ExtractionSettings | None = None,
    debug: bool = False,
) -> dict[str, Any]:
    """
    Run the full advisor stack.

    Parameters
    ----------
    front_img, side_img:
        File paths or BGR/RGB/gray `numpy.ndarray` images.
    model:
        Path to `size_model.joblib` or a loaded `SizeModelBundle`.
    height_cm:
        Subject height for pixel→cm calibration (measured or from metadata).

    Returns
    -------
    dict with keys: top_size, bottom_size, fit, confidence, measurements, features, feature_vector
    """
    bundle = load_bundle(model) if not isinstance(model, SizeModelBundle) else model

    ex = extract_full(front_img, side_img, height_cm=height_cm, settings=settings)
    feats = build_features_from_extraction(ex, settings=settings)
    order = bundle.feature_order
    x = features_dict_to_vector(feats, feature_order=order).reshape(1, -1)

    if debug:
        print("[predict_size] feature_vector (name: value):")
        for j, name in enumerate(order):
            print(f"  {name}: {float(x[0, j]):.6f}")

    y_hat = bundle.estimator.predict(x)[0]
    out = decode_predictions(bundle, y_hat)
    try:
        head_conf = per_head_max_proba(bundle, x)
        conf = float(np.mean(head_conf))
        c_top, c_bot, c_fit = head_conf[0], head_conf[1], head_conf[2]
        if debug:
            print(
                f"[predict_size] predict_proba max per head: top={c_top:.4f} bottom={c_bot:.4f} fit={c_fit:.4f}"
            )
    except Exception as e:
        if debug:
            print(f"[predict_size] predict_proba failed: {e}")
        conf = 1.0
        c_top = c_bot = c_fit = 1.0

    return {
        "top_size": out["top_size"],
        "bottom_size": out["bottom_size"],
        "fit": out["fit"],
        "confidence": conf,
        "confidence_top": float(c_top),
        "confidence_bottom": float(c_bot),
        "confidence_fit": float(c_fit),
        "measurements": dict(ex.measurements),
        "features": feats,
        "feature_vector": features_dict_to_vector(feats, feature_order=order),
    }
