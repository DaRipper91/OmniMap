# Technical Stack: OmniMap Native

## 🏗️ Architecture
- **Pattern:** MVVM + Clean Architecture (Domain, Data, UI).
- **Language:** Kotlin 2.0+ (Strictly idiomatic).
- **UI Framework:** Jetpack Compose (Optimized for high refresh rates).

## 💾 Data & Persistence
- **Database:** Room Persistence Library (Reactive with Kotlin Flow).
- **Graph Logic:** Custom Kotlin `GraphProcessor` optimized for O(log n) traversal.

## 🤖 Intelligence Layer
- **Core Engine:** Google Gemini 1.5 Pro SDK (Official Android SDK).
- **Fallback/Hybrid:** Local Ollama (Pattern B) for offline-critical logic.
- **Architect Model:** `omnimap-architect` (Principal persona enforcing structured JSON outputs).
- **Mutation Pipeline:** Automatic JSON parsing in `GraphViewModel` translates AI architectural plans into real-time Room mutations.

## 📊 Infrastructure
- **GitLab:** Primary source of truth (`git@gitlab.com:DaRipper91/OmniMap.git`).
- **GitHub:** Mirror/Backup (`https://github.com/DaRipper91/OmniMap.git`).
