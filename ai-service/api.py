from fastapi import FastAPI, UploadFile, File, Form
import numpy as np
import cv2
from pipeline.predict import predict_size
from pipeline.model import load_bundle

app = FastAPI()

# Load model once at startup
model = load_bundle("models/size_model.joblib")


def read_image(file: UploadFile):
    contents = file.file.read()
    np_arr = np.frombuffer(contents, np.uint8)
    img = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)
    return img


@app.get("/")
def root():
    return {"message": "AI Service Running"}


@app.get("/health")
def health():
    return {"status": "OK"}


@app.post("/predict")
async def predict(
    front: UploadFile = File(...),
    side: UploadFile = File(...),
    height_cm: float = Form(...)
):
    try:
        front_img = read_image(front)
        side_img = read_image(side)

        result = predict_size(
            front_img,
            side_img,
            model,
            height_cm=height_cm
        )

        return {
            "top_size": result["top_size"],
            "bottom_size": result["bottom_size"],
            "fit": result["fit"],
            "confidence": {
                "top": result["confidence_top"],
                "bottom": result["confidence_bottom"],
                "fit": result["confidence_fit"]
            },
            "measurements": result["measurements"]
        }

    except Exception as e:
        return {"error": str(e)}