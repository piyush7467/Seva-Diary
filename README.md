# Seva Diary — सेवा डायरी

A production-grade, devotional personal companion app and backend suite for devotees of **Satguru Rampal Ji Maharaj**, maintaining sacred spiritual records with reverence, privacy, and precision.

---

## 🌟 Key Features

### 📿 Daily Naam Jaap Tracker
- **Tactile Circular Counter**: Interactive circular progress dial with target tracking and spring-bounce push button.
- **7 Sacred Mandalas**: Devotional 3-row layout featuring Satguru Rampal Ji Maharaj & Supreme Kabir Sahib (`img1`) and the sacred lotus chakras (`img2`–`img7`).
- **Mala Beads Tracking**: Automated 108-bead Mala counter and progress calculations.
- **Audio & Haptic Feedback**: Click sound toggle (with instant mute mode for silent chanting) and responsive vibration.

### 🏛️ Satlok Ashram Visit Diary
- **Verified Master Data**: Accurate directory of Satlok Ashrams (Barwala, Karontha, Bhiwani, Shamli, Betul, Dhanaana, Mundka, etc.).
- **Inclusive Duration Math**: Exact calculation ($endDate - startDate + 1$) reflecting Indian devotional calendar practice.
- **Seva & Memorabilia**: Categorized seva duties, spiritual notes, and search/filter.

### 📄 Local PDF Report Generator
- **Zero-Cloud Privacy**: Native on-device PDF generation using Android Canvas & `PdfDocument`. No records or documents are ever uploaded to any server.
- **Personalized Devotional Format**: Features the sacred logo, devotee name, and clean tables for Naam Jaap and Ashram visits.
- **Android Sharesheet**: Immediate export via `FileProvider` to WhatsApp, Drive, or Print.

### 👤 Devotee Authentication & Onboarding
- **Devotional Auto-Naming**: Automatically suggests devotional names (`FirstName Das` / `FirstName Dasi`).
- **Mantra Initiation Stages**: Dedicated selection for *Pratham Nam* (प्रथम नाम), *Satnam* (सतनाम), and *Sarnam* (सारनाम).
- **Searchable Location Directory**: Interactive search filter covering all 36 States & Union Territories of India and their respective districts.
- **Dual-Language & Devotional Theme**: English and Hindi support with Warm Ivory (`#FDFBF7`), Forest Green (`#1E4D2B`), and Saffron Gold (`#D9822B`).

---

## 🏗️ Architecture

```
seva_diary/
├── android/               # Android App (Kotlin + Jetpack Compose)
│   ├── app/src/main/
│   │   ├── java/.../app/
│   │   │   ├── core/      # Design system, network, security, PDF engine, location directory
│   │   │   ├── feature/   # Auth, Chanting, Ashram Diary, Reports, Settings
│   │   │   └── navigation/# Compose Navigation graph
│   │   └── res/           # Drawables (logo, img1–7, custom devotional vector suite), strings (en/hi)
│   └── build.gradle.kts
└── backend/               # Modular Monolith (Node.js + Express)
    ├── src/
    │   ├── modules/       # Auth, Users, Chanting, Seva, Master Data
    │   ├── integrations/  # Brevo transactional email SDK
    │   └── common/        # Middleware, security rate-limiters, token utilities
    └── api/index.js       # Vercel Serverless & standalone HTTP entrypoint
```

---

## 🚀 Getting Started

### 1. Prerequisites
- **Android Studio** (Koala / Ladybug or newer) with Android SDK 35+
- **JDK 21**
- **Node.js** 18+ & **npm**

### 2. Backend Setup
```bash
cd backend
npm install
cp .env.example .env   # Configure your MongoDB Atlas URI & Brevo API Key
npm run dev            # Starts server on http://localhost:5000
npm test               # Run automated Jest test suite
```

### 3. Android Setup
1. Open the `android/` directory in Android Studio.
2. Allow Gradle sync to complete.
3. Run on an emulator or connect your Android device with USB debugging enabled:
```powershell
cd android
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

---

## 🔒 Security & Privacy
- **AES-256 GCM**: Android Keystore encrypted storage for sensitive session tokens.
- **Zero PDF Uploads**: Reports are compiled directly on the client.
- **Strict Rate Limiting**: Centralized brute-force protection on all authentication endpoints.
