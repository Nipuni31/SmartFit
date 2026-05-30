# SmartFit | AI-Powered Clothing Size Recommendation Platform

SmartFit is an AI-powered microservices-based system that estimates human body measurements from front and side images and predicts accurate clothing sizes and fit recommendations in real time. It combines computer vision, machine learning, and secure backend services to provide an end-to-end intelligent fashion sizing solution.

---

##  Features

- Upload front and side body images for analysis  
- Automatic body measurement extraction using computer vision  
- AI-based clothing size prediction (Top, Bottom, Fit)  
- Confidence scoring for predictions  
- User authentication and role-based access (Buyer / Tailor / Admin)  
- Prediction history storage and retrieval  
- Real-time API-based microservices communication  
- Interactive frontend for visualization and results  

---

##  System Architecture

- **Frontend**: User interface for image upload and result visualization  
- **API Gateway**: Routes requests between frontend, AI service, and user service  
- **User Service**: Handles authentication, authorization, and user management  
- **AI Service**: Processes images, extracts measurements, and runs ML models for prediction  
- **Database**: Stores user data, profiles, and prediction history  

---

##  How It Works

1. User registers and logs in to the system  
2. Front and side images are uploaded through the frontend  
3. API Gateway forwards request to AI Service  
4. AI Service extracts body measurements using computer vision  
5. Machine learning model predicts clothing size and fit  
6. Results are returned and stored in the database  
7. User views size recommendation with confidence scores  

---

## AI/ML Pipeline

- Image preprocessing and segmentation using OpenCV & MediaPipe  
- Feature engineering from body silhouette geometry  
- Random Forest-based classification model  
- Outputs:
  - Top Size
  - Bottom Size
  - Fit Category
  - Confidence Scores  

---

## Tech Stack

Python, OpenCV, MediaPipe, scikit-learn, FastAPI, Streamlit, Java Spring Boot, Spring Security, JWT, MongoDB, REST APIs, Microservices Architecture

---

##  Project Structure
SmartFit/
│
├── ai-service/ # Python AI/ML pipeline (FastAPI)
├── user-service/ # Spring Boot authentication service
├── api-gateway/ # API Gateway service
├── frontend/ # React/Vite frontend
└── README.md

##  Screenshots

### 🔹 Measurement Visualization (Front View)

<img width="658" height="799" alt="image" src="https://github.com/user-attachments/assets/843a3026-7f07-4ad5-bc44-5b94df66040d" />


---

### 🔹 Measurement Visualization (Side View)

<img width="681" height="810" alt="image" src="https://github.com/user-attachments/assets/c0670749-645b-4a8f-b2af-ac452d7b0498" />


---

##  Security

- JWT-based authentication  
- Role-based access control (Buyer, Tailor, Admin)  
- Secure REST API communication between services  

---

## Future Improvements

- Deploy microservices using Docker & Kubernetes  
- Improve ML model with deep learning (CNN-based body estimation)  
- Add real-time video-based measurement  
- Cloud database integration  
- Mobile application support  

---



##  Machine Learning

* Model: **Random Forest (MultiOutputClassifier)**
* Predicts:

  * Top size
  * Bottom size
  * Fit

### Train Model

```bash
python scripts/train_model.py --csv Data/sample_measurements.csv --out models/size_model.joblib
```

---

##  Usage

### Install dependencies

```bash
pip install -r requirements.txt
```

---

### Run CLI

```bash
python main.py \
  --front Data/train/000001/mask/front.png \
  --side Data/train/000001/mask/side.png \
  --model models/size_model.joblib \
  --height-cm 175
```

---

### Run Web App

```bash
streamlit run app.py
```

---

##  Example Output

```text
Top Size: XL
Bottom Size: XL
Fit: Tight
Confidence: 100%
```

---

##  Technologies

* Python
* OpenCV
* NumPy & Pandas
* Scikit-learn
* Streamlit

---

##  Limitations

* Uses rule-generated labels (not real clothing data)
* Sensitive to image quality
* Needs balanced dataset for better ML performance

---

##  Future Work

* Train with real size labels
* Add deep learning (CNN)
* Improve fit recommendation

---
## Author

Developed as a full-stack AI + microservices project demonstrating computer vision, machine learning, and scalable backend architecture.

