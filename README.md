# 🏠 AI-Enhanced Domestic Helper Hiring App

An Android application designed to streamline the domestic helper hiring process 
for Hong Kong employers — built as a Final Year Project at Hong Kong Metropolitan 
University (2024–2025).

---

## 📱 Overview

Finding and hiring a domestic helper in Hong Kong involves significant manual effort 
— reviewing resumes, scheduling interviews, and managing communication across 
language barriers. This app automates and simplifies that process using AI-powered 
matching, real-time translation, and integrated video interviewing.

---

## ✨ Features

- 🤖 **AI Candidate Matching** — Hybrid rule-based + ML system that parses PDF 
  resumes, extracts skills and languages, and automatically matches candidates to 
  employers
- 💬 **Real-Time Chat** — Firebase-powered instant messaging between employers 
  and helpers
- 🌐 **Multilingual Support** — Real-time translation across 4 languages via 
  MyMemory API
- 📹 **Video Interviews** — Integrated Jitsi Meet for in-app video calls
- 📅 **Booking Management** — End-to-end interview and appointment scheduling
- 🔐 **Role-Based Access** — Separate flows for employers, helpers, and admins
- 🔍 **Advanced Search & Filtering** — Filter candidates by skills, language, 
  experience, and availability

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin (Coroutines) |
| IDE | Android Studio |
| Backend & Auth | Firebase (Auth, Realtime Database, Cloud Messaging) |
| ML & Matching | scikit-learn, Chaquopy |
| Resume Parsing | pdfplumber |
| Translation | MyMemory API |
| Video Calls | Jitsi Meet SDK |

---

## 🧠 How the Matching System Works

1. Helper uploads their CV (PDF)
2. `pdfplumber` extracts raw text from the document
3. Skills, languages, and experience are parsed and structured
4. A hybrid rule-based + `scikit-learn` ML model scores compatibility 
   against employer requirements
5. Ranked matches are surfaced to the employer in real time

---


## 🚀 Getting Started

### Prerequisites
- Android Studio (latest stable)
- Android device or emulator (API 26+)
- Firebase project with Realtime Database and Auth enabled

### Setup
1. Clone the repo
```bash
   git clone https://github.com/singhRamandeep101/Domestic-helper-app.git
```
2. Open in Android Studio
3. Add your `google-services.json` from Firebase Console to `/app`
4. Add your MyMemory API key to `local.properties`
5. Run on emulator or device

---

## 👥 Team

Built by a 4-person team as a Final Year Project at HKMU.
Project Lead & Full-Stack Developer: **Ramandeep Singh**

---

## 📄 License

This project is for academic purposes. Contact singhramandeep0910@gmail.com 
for enquiries.
