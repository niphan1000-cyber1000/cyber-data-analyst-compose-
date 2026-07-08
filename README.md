# AI Analytics Dashboard

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Compose-Material%203-purple.svg)](https://developer.android.com/jetpack/compose)
[![Gemini AI](https://img.shields.io/badge/Gemini-3.5%20Flash-orange.svg)](https://ai.google.dev)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**แอปวิเคราะห์ข้อมูลด้วย AI** รองรับการอัปโหลดไฟล์ CSV/Excel และถามคำถามด้วย**ภาษาไทยธรรมชาติ** เพื่อสร้าง KPI, แผนภูมิแบบ interactive และข้อมูลเชิงลึกอัตโนมัติ

---

## 📋 สารบัญ
- [ภาพรวม](#ภาพรวม)
- [คุณสมบัติหลัก](#คุณสมบัติหลัก)
- [ภาพหน้าจอ](#ภาพหน้าจอ)
- [Tech Stack](#tech-stack)
- [การติดตั้งและรัน](#การติดตั้งและรัน)
- [โครงสร้างโปรเจกต์](#โครงสร้างโปรเจกต์)
- [การใช้งาน](#การใช้งาน)
- [Security & Production](#security--production-notes)
- [Roadmap](#roadmap)
- [License](#license)

---

## 🧭 ภาพรวม

**AI Analytics** เป็นแอปพลิเคชัน Android ที่ช่วยให้ผู้ใช้ทั่วไปหรือนักวิเคราะห์ข้อมูลสามารถ:
- อัปโหลดไฟล์ CSV หรือวางข้อมูล
- ถามคำถามด้วยภาษาไทยธรรมชาติ (เช่น "ยอดขายรวมปีนี้เท่าไหร่", "สินค้าที่ขายดีที่สุดคืออะไร")
- ได้ผลลัพธ์แบบอัจฉริยะ: KPI, แผนภูมิ (Bar, Line, Pie), ข้อมูลเชิงลึก และ SQL Query

---

## ✨ คุณสมบัติหลัก

- **รองรับภาษาไทยเต็มรูปแบบ** — ถาม-ตอบด้วยภาษาไทยธรรมชาติ
- **แผนภูมิแบบ Interactive** — Bar, Line, Pie, Area
- **KPI Cards** — แสดงตัวชี้วัดสำคัญพร้อมแนวโน้ม
- **ประวัติการวิเคราะห์** — เก็บผลการวิเคราะห์ทั้งหมด
- **Dataset Templates** — มีตัวอย่างข้อมูลพร้อมใช้ (ยอดขาย, Traffic, Finance, Inventory)
- **Room Database** — เก็บข้อมูลถาวร
- **Offline Support** — แดชบอร์ดและข้อมูลเก่ายังใช้งานได้

---

## 📱 ภาพหน้าจอ

(เพิ่มรูปภาพหน้าจอที่นี่)

---

## 🛠️ Tech Stack

- **UI**: Jetpack Compose + Material 3 (Dark Elegant Theme)
- **AI**: Google Gemini 3.5 Flash (JSON Mode)
- **Database**: Room + Flow
- **Networking**: Retrofit + OkHttp + Moshi
- **State Management**: ViewModel + Coroutines
- **Testing**: Robolectric + Roborazzi

---

## 🚀 การติดตั้งและรัน

### Prerequisites
- Android Studio (Latest)
- Gemini API Key

### ขั้นตอน
1. Clone หรือเปิดโปรเจกต์ใน Android Studio
2. คัดลอก `.env.example` → `.env`
3. ใส่ `GEMINI_API_KEY` ในไฟล์ `.env`
4. Sync Gradle
5. รันแอปบน Emulator หรืออุปกรณ์จริง

---

## 📁 โครงสร้างโปรเจกต์
app/src/main/java/com/example/
├── data/
│   ├── db/                  # Room Database + Entities
│   ├── model/               # Data Models + Templates
│   └── repository/          # AnalyticsRepository
├── ui/
│   ├── screens/             # MainScreen, Chart components
│   └── theme/               # Custom Dark Theme
├── MainActivity.kt
└── ui/viewmodel/            # AnalyticsViewModel
text---

## 🔐 Security & Production Notes

> **⚠️ สำคัญ**: API Key ถูกฝังใน APK  
> **ห้าม** แจก APK สาธารณะในเวอร์ชันปัจจุบัน

**แนะนำสำหรับ Production**:
- ใช้ **Backend Proxy** (Cloud Functions)
- ใช้ **Firebase App Check**
- เก็บ Key ใน Secret Manager

---

## 🗺️ Roadmap

- [x] Core UI + Gemini Integration
- [x] Room Database + History
- [ ] Real File Upload (CSV/Excel)
- [ ] Chart Export (PNG/PDF)
- [ ] Backend Proxy Support
- [ ] Google Play Ready Build

---

## 📄 License

MIT License — ดูรายละเอียดใน [LICENSE](LICENSE)

---

**พัฒนาโดย AI Analytics Team**  
ติดต่อหรือรายงานปัญหา: [GitHub Issues](https://github.com/...)
