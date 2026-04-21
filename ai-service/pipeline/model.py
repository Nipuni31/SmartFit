"""
Load / save trained multi-output size models (joblib bundles).

Designed so you can swap the inner sklearn estimator (e.g. GradientBoosting, CNN features later)
without changing the CLI contract.
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
from typing import Any

import joblib
import numpy as np
from sklearn.preprocessing import LabelEncoder


@dataclass
class SizeModelBundle:
    """
    `estimator` is typically `MultiOutputClassifier(RandomForestClassifier(...))`.
    Three label heads: top_size, bottom_size, fit_type (order fixed).
    """

    estimator: Any
    encoders: tuple[LabelEncoder, LabelEncoder, LabelEncoder]
    feature_order: tuple[str, ...]
    meta: dict[str, Any]

    HEAD_NAMES: tuple[str, ...] = ("top_size", "bottom_size", "fit")


def save_bundle(path: str | Path, bundle: SizeModelBundle) -> None:
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    payload = {
        "estimator": bundle.estimator,
        "encoders": bundle.encoders,
        "feature_order": list(bundle.feature_order),
        "meta": bundle.meta,
    }
    joblib.dump(payload, path)


def load_bundle(path: str | Path) -> SizeModelBundle:
    path = Path(path)
    if not path.is_file():
        raise FileNotFoundError(f"Model bundle not found: {path}")
    raw: dict[str, Any] = joblib.load(path)
    encoders = raw["encoders"]
    if not isinstance(encoders, (list, tuple)) or len(encoders) != 3:
        raise ValueError("Bundle must contain three LabelEncoders (top, bottom, fit).")
    fo = raw.get("feature_order")
    if fo is None:
        raise ValueError("Bundle missing feature_order.")
    return SizeModelBundle(
        estimator=raw["estimator"],
        encoders=(encoders[0], encoders[1], encoders[2]),
        feature_order=tuple(fo),
        meta=dict(raw.get("meta", {})),
    )


def predict_encoded_matrix(bundle: SizeModelBundle, X: np.ndarray) -> np.ndarray:
    """X shape (1, n_features) or (n, n_features). Returns integer matrix (n, 3)."""
    return bundle.estimator.predict(X)


def predict_proba_list(bundle: SizeModelBundle, X: np.ndarray) -> list[np.ndarray]:
    """Delegates to underlying MultiOutputClassifier when available."""
    est = bundle.estimator
    if not hasattr(est, "predict_proba"):
        raise AttributeError("Estimator has no predict_proba (needed for confidence).")
    return est.predict_proba(X)


def mean_max_proba_confidence(bundle: SizeModelBundle, X: np.ndarray) -> float:
    """
    Average across heads of max class probability (calibrated-ish score for display).
    """
    scores = per_head_max_proba(bundle, X)
    return float(np.mean(scores)) if scores else 0.0


def per_head_max_proba(bundle: SizeModelBundle, X: np.ndarray) -> list[float]:
    probas = predict_proba_list(bundle, X)
    return [float(np.max(p, axis=1)[0]) for p in probas]


def decode_predictions(bundle: SizeModelBundle, y_hat: np.ndarray) -> dict[str, str]:
    """y_hat shape (3,) encoded ints."""
    top = bundle.encoders[0].inverse_transform([int(y_hat[0])])[0]
    bottom = bundle.encoders[1].inverse_transform([int(y_hat[1])])[0]
    fit = bundle.encoders[2].inverse_transform([int(y_hat[2])])[0]
    return {"top_size": str(top), "bottom_size": str(bottom), "fit": str(fit)}
