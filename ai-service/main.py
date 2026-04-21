"""
Smart Clothing Size Advisor — CLI entry point.

Example:
  python main.py --front front.png --side side.png --model models/size_model.joblib --height-cm 175
"""

from __future__ import annotations

import argparse
from pathlib import Path

import cv2
import mediapipe as mp

mp_selfie = mp.solutions.selfie_segmentation

from pipeline.model import load_bundle
from pipeline.predict import predict_size
from pipeline.visualize import build_visualization_pair, save_visualization_pair


def _fmt_fit(fit: str) -> str:
    return fit.replace("_", " ").title()


def main() -> None:
    root = Path(__file__).resolve().parent
    p = argparse.ArgumentParser(description="Smart Clothing Size Advisor")
    p.add_argument("--front", type=Path, required=True, help="Front-view silhouette or photo")
    p.add_argument("--side", type=Path, required=True, help="Side-view silhouette or photo")
    p.add_argument(
        "--model",
        type=Path,
        default=root / "models" / "size_model.joblib",
        help="Trained joblib bundle (train via scripts/train_model.py)",
    )
    p.add_argument("--height-cm", type=float, default=None, help="Subject height for calibration")
    p.add_argument(
        "--csv",
        type=Path,
        default=None,
        help="Optional: read height from this CSV at --csv-row (if --height-cm omitted)",
    )
    p.add_argument("--csv-row", type=int, default=0)
    p.add_argument("--save-viz", type=Path, default=None, help="Directory to write overlay PNGs")
    p.add_argument("--no-show", action="store_true", help="Skip matplotlib display when saving viz")
    p.add_argument("--debug", action="store_true", help="Print feature vector and proba diagnostics")

    args = p.parse_args()

    height_cm = args.height_cm
    if height_cm is None:
        csv_path = args.csv
        if csv_path is None:
            for cand in (root / "data" / "measurements.csv", root / "Data" / "measurements.csv"):
                if cand.is_file():
                    csv_path = cand
                    break
        if csv_path is None or not csv_path.is_file():
            raise SystemExit("Provide --height-cm or a valid --csv path with a height column.")
        import pandas as pd

        df = pd.read_csv(csv_path)
        height_cm = float(df["height"].values[args.csv_row])

    bundle = load_bundle(args.model)
    result = predict_size(args.front, args.side, bundle, height_cm=height_cm, debug=args.debug)

    top = result["top_size"]
    bottom = result["bottom_size"]
    fit = result["fit"]
    fit_disp = _fmt_fit(fit)
    pct_top = int(round(result.get("confidence_top", result["confidence"]) * 100))
    pct_bot = int(round(result.get("confidence_bottom", result["confidence"]) * 100))

    print()
    print(f"Top: {top} ({fit_disp} Fit) – {pct_top}%")
    print(f"Bottom: {bottom} ({fit_disp} Fit) – {pct_bot}%")
    print()
    print("Measurements (cm):")
    for k, v in result["measurements"].items():
        print(f"  {k}: {v:.2f}")

    summary = f"Top {top} | Bottom {bottom} | {fit_disp} | ~{int(round(result['confidence'] * 100))}%"

    if args.save_viz:
        out_dir = Path(args.save_viz)
        out_dir.mkdir(parents=True, exist_ok=True)
        front_vis, side_vis = build_visualization_pair(
            args.front,
            args.side,
            height_cm=height_cm,
            prediction_summary=summary,
        )
        save_visualization_pair(
            front_vis,
            side_vis,
            out_dir / "front_overlay.png",
            out_dir / "side_overlay.png",
        )
        print(f"\nSaved overlays -> {out_dir}")

    if not args.no_show:
        try:
            import matplotlib.pyplot as plt

            front_vis, side_vis = build_visualization_pair(
                args.front,
                args.side,
                height_cm=height_cm,
                prediction_summary=summary,
            )
            plt.figure(figsize=(8, 8))
            plt.imshow(cv2.cvtColor(front_vis, cv2.COLOR_BGR2RGB))
            plt.title("Front — " + summary)
            plt.axis("off")
            plt.figure(figsize=(8, 8))
            plt.imshow(cv2.cvtColor(side_vis, cv2.COLOR_BGR2RGB))
            plt.title("Side — depths")
            plt.axis("off")
            plt.show()
        except Exception as exc:
            print(f"(Skipping interactive display: {exc})")


if __name__ == "__main__":
    main()
