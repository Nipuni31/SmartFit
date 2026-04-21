"""
Smart Clothing Size Advisor — modular CV + ML pipeline.
"""

from .extract import ExtractionResult, extract_full, extract_measurements
from .features import FEATURE_ORDER, build_features_from_extraction, features_dict_to_vector
from .model import SizeModelBundle, load_bundle, save_bundle
from .predict import predict_size
from .settings import DEFAULT_EXTRACTION_SETTINGS, ExtractionSettings

__all__ = [
    "DEFAULT_EXTRACTION_SETTINGS",
    "ExtractionResult",
    "ExtractionSettings",
    "FEATURE_ORDER",
    "SizeModelBundle",
    "build_features_from_extraction",
    "extract_full",
    "extract_measurements",
    "features_dict_to_vector",
    "load_bundle",
    "predict_size",
    "save_bundle",
]
