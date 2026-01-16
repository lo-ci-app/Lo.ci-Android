# Loci - Android

## 📱 Project Overview
Loci iOS 앱의 안드로이드 버전 프로젝트입니다.
사용자의 위치 기반 경험과 친구와의 연결을 제공하는 소셜 애플리케이션입니다.

## 🛠 Tech Stack

### Architecture
- **Language:** Kotlin
- **UI:** Jetpack Compose (Material3)
- **Architecture Pattern:** MVVM (Model-View-ViewModel) + Clean Architecture (Recommended)

### Key Libraries
- **Network:** Retrofit2 + OkHttp + Kotlin Serialization
- **Image Loading:** Coil
- **Map:** Google Maps SDK for Android (Maps Compose)
- **Camera:** CameraX
- **Bluetooth:** Android BLE / Nordic BLE Library
- **Concurrency:** Coroutines + Flow
- **Dependency Injection:** Hilt (Recommended)

## 📂 iOS Porting Reference
이 프로젝트는 기존 [Loci-iOS] 리포지토리를 기반으로 포팅되었습니다.
주요 기능(카메라, 지도, 블루투스 스캔 등)은 안드로이드 네이티브 API로 재구현됩니다.

## 🚀 Setup & Build
1. `local.properties`에 `sdk.dir` 설정이 되어 있는지 확인하세요.
2. `google-services.json` 파일이 필요할 경우 추가하세요.
3. Android Studio (Ladybug 이상)에서 프로젝트를 엽니다.