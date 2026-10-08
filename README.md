# Cascade Study (v1.0.0)

**Cascade Study** is a modern Android study-time management application based on the **cascade study technique**.

Instead of studying in fixed-length blocks, a session is divided into **progressively shorter study intervals** separated by rest periods. This technique prevents mental fatigue and makes long study sessions easier to maintain.

Designed and developed by **[Daniel Culpian](https://github.com/DanielCulpian)**.

---

## 🌟 Key Features

* 🎯 **Cascade Study Engine:** Automatically coordinates study and rest intervals with smooth state transitions (`IDLE`, `STUDYING`, `RESTING`, `PAUSED`, `FINISHED`).
* ⚡ **Preset Configurations:**
  * **Full Session (`FULL`):** `60 → 50 → 40 → 30 → 20 → 10` minutes with 10-minute rest breaks (Total: 4h 20m).
  * **Short Session (`SHORT`):** `30 → 20 → 10` minutes with 10-minute rest breaks (Total: 1h 20m).
* 📊 **Study Statistics & Local Room Database:**
  * Local persistence powered by **Room (SQLite)**.
  * Live Material 3 statistics card displaying **Total Historical Study Time** and **Weekly Study Time** formatted in exact hours and minutes (e.g., `1h 25m`).
* 🔔 **Custom Notifications & Sound Effects:**
  * Heads-up Android notifications for interval starts, rest breaks, and full session completions (with Android 13+ `POST_NOTIFICATIONS` runtime permission support).
  * Custom audio cues (`start_study_interval.mp3`, `finish_study_interval.mp3`, and `finish_session.mp3`).
* 🏁 **Early Session Completion & Cancellation:**
  * Finish a session at any time with the **"Finalizar"** button. The exact accumulated study time is calculated and saved to the database.
  * System back gesture integration (`BackHandler`) resets the timer and safely returns to the session selection screen.
* 🌓 **Adaptive UI & State Preservation:**
  * Declarative **Material 3** Jetpack Compose interface supporting light/dark theme toggles and screen rotations without losing state (`rememberSaveable`).

---

## 📐 Architecture & Project Structure

The application follows **Clean Architecture** principles combined with the **MVVM** pattern and reactive data streams using Kotlin **Coroutines** and **StateFlow**.

```text
com.example.cascadestudy
├── MainActivity.kt                      // Entry point, ViewModel factory, permission handling
│
├── data
│   ├── local
│   │   ├── AppDatabase.kt               // Room database singleton instance
│   │   ├── CompletedSessionDao.kt       // Data Access Object with reactive SQL flows
│   │   └── CompletedSessionEntity.kt    // SQLite entity table for completed sessions
│   └── repository
│       └── SessionRepository.kt         // Data repository with weekly time calculations
│
├── domain
│   ├── CascadeTimer.kt                  // Core timer controller & time calculations
│   ├── StudySession.kt                  // Domain data class representing session intervals
│   ├── StudySessionPreset.kt            // Enum defining available session presets
│   ├── StudySessionPresetExtensions.kt  // Mapping extension for presets to domain models
│   ├── SystemTimerClock.kt              // Production system clock implementation
│   ├── TimerClock.kt                    // Interface abstraction for time
│   ├── TimerEvent.kt                    // Sealed interface for state machine events
│   ├── TimerState.kt                    // Enum defining timer operational states
│   └── TimerStateMachine.kt             // Encapsulated state transition rules
│
├── notification
│   └── NotificationHelper.kt            // Android NotificationChannel and notification manager
│
├── presentation
│   ├── AppScreen.kt                     // Navigation screen enumeration
│   ├── components
│   │   └── AuthorCredits.kt             // Clickable author credit component
│   ├── selection
│   │   └── SessionSelectionScreen.kt    // Compose screen for choosing session presets
│   └── timer
│       ├── TimerScreen.kt               // Main timer screen layout and UI controls
│       ├── TimerUiState.kt              // Immutable state data holder for UI
│       └── TimerViewModel.kt            // ViewModel coordinating state, timer, audio, and DB
│
├── sound
│   └── SoundManager.kt                  // Audio player managing MediaPlayer effects
│
└── ui
    └── theme                            // Material 3 theme, colors, and typography
```

---

## 🔁 App Navigation & Workflow

```text
  ┌───────────────────────────┐
  │  SessionSelectionScreen   │
  │  (Choose FULL / SHORT)    │
  └─────────────┬─────────────┘
                │ Select Preset
                ▼
  ┌───────────────────────────┐
  │        TimerScreen        │
  │  (Countdown, Stats, &     │
  │   Audio/Notification)     │
  └─────────────┬─────────────┘
                │ Click "Finalizar" / Finish
                ▼
  ┌───────────────────────────┐
  │ Saves time to Room DB     │
  │ Plays Sound & Notification│
  │ Returns to Selection      │
  └───────────────────────────┘
```

---

## 🧪 Testing Strategy

Quality and testability are fundamental to Cascade Study. The project contains a comprehensive suite of unit and integration tests:

* **`CascadeTimerTest`**: Verifies state machine transitions, exact interval countdowns, time preservation on pause/resume, and elapsed time calculations.
* **`StudySessionPresetTest` & `StudySessionTest`**: Validates preset interval configurations and parameter bounds.
* **`TimerViewModelTest`**: Tests UI state flows, coroutine tickers, database persistence triggers, notification triggers, and audio triggers using `FakeCompletedSessionDao`, `FakeNotificationHelper`, and `FakeSoundManager`.
* **`SessionRepositoryTest`**: Verifies repository Flow mappings, weekly start-of-week timestamp calculations, and database insertions.
* **`CompletedSessionDaoTest`**: Instrumented Room database tests verifying SQLite queries on Android runtime.

---

## 🛠️ Tech Stack

| Component | Technology | Purpose |
| --- | --- | --- |
| **Language** | Kotlin (v2.4+) | Primary language |
| **UI Framework** | Jetpack Compose | Declarative UI |
| **Design System** | Material 3 | Modern Android styling & adaptive themes |
| **Architecture** | MVVM + Clean Architecture | Separation of concerns & state management |
| **Asynchrony** | Kotlin Coroutines & StateFlow | Reactive state updates & ticker timers |
| **Database** | Room (v2.8+) + KSP | Local SQLite persistence & reactive queries |
| **Audio & Media** | MediaPlayer | Custom audio effect playback |
| **Notifications** | Android NotificationManagerCompat | Heads-up system alerts |
| **Unit Testing** | JUnit 4 + Coroutines Test | Automated unit & integration testing |
| **Build System** | Gradle (Kotlin DSL) | Build configuration & dependency management |

---

## 👤 Author & Credits

Designed and developed by **Daniel Culpian**.

* **GitHub:** [https://github.com/DanielCulpian](https://github.com/DanielCulpian)

---

## 📄 License

Cascade Study v1.0.0 — All rights reserved.
