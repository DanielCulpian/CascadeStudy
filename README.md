# Cascade Study (v2.0.1)

**Cascade Study** is a modern Android study-time management application built around the **cascade study technique**.

Instead of studying in fixed-length blocks, a session is divided into **progressively shorter study intervals** separated by 10-minute rest breaks. This technique maintains peak focus, prevents mental burnout, and optimizes long-term learning efficiency.

Designed and developed by **[Daniel Culpian](https://github.com/DanielCulpian)**.

---

## 🌟 Key Features (v2.0.1)

* 🎯 **Cascade Study Engine:** Coordinates study and rest intervals with smooth state transitions (`IDLE`, `STUDYING`, `RESTING`, `PAUSED`, `FINISHED`).
* ⚡ **Preset Configurations:**
  * **Full Session (`FULL`):** `60 → 50 → 40 → 30 → 20 → 10` minutes with 10-minute rest breaks (Total: 4h 20m).
  * **Short Session (`SHORT`):** `30 → 20 → 10` minutes with 10-minute rest breaks (Total: 1h 20m).
* 💾 **Incremental Interval Persistence (Room DB):**
  * Automatically saves every completed study interval directly to **Room SQLite** as soon as it finishes.
  * Protects study progress against unexpected app closes or crashes.
  * Early session cancellation ("Finalizar") calculates and saves elapsed time for the active interval only without double counting.
* 📊 **Triple Statistics Display Card (`StudyStatisticsCard`):**
  * Displays **Total Histórico**, **Esta Semana**, and **Hoy** side-by-side in real time on both the main selection screen and active timer view.
  * Daily counters reset automatically every midnight (`00:00:00`).
* 📅 **Weekly History View & Advanced Filtering (`WeeklyHistoryScreen`):**
  * View a closed weekly table (Monday to Sunday) with a smooth scrollable list (`LazyColumn`).
  * Weeks with 0 study hours are recorded and displayed (e.g. `"2-8 de Diciembre/2026", Horas realizadas = 0.0 h`).
  * Filter weekly records by **Month**, **Year**, and **Study Hours** threshold (`≥ X h`, `≤ X h`, slider).
  * Aesthetic empty state cards when no records exist.
* ℹ️ **Interactive Method Explanation Pop-up:**
  * Information icon button on the main view opening an `AlertDialog` detailing the Cascade Study method and session durations.
* 🎨 **Dynamic State Colors & Aesthetic Author Badge:**
  * Dynamic status badge colors: Soft forest green for `STUDYING`, soft burgundy red for `RESTING`, primary theme for `IDLE`/`FINISHED`.
  * Sleek Material 3 pill-shaped author badge (`AuthorCredits`) featuring the official GitHub logo redirecting to [Daniel Culpian's GitHub](https://github.com/DanielCulpian).
* ⏰ **Zero-Lag Exact Alarm Background System (`AlarmManager.setAlarmClock`):**
  * Uses system kernel alarms (`setAlarmClock`) for exact background interval transitions, audio cues, and heads-up notifications with **0% System UI lag, 0% CPU overhead, and 0 background battery drain**.
  * Swipeable/dismissible interval transition notifications.
* 📱 **Orientation Lock & SingleTop Navigation:**
  * Locked to portrait orientation (`android:screenOrientation="portrait"`).
  * SingleTop launch mode preserves app state when opening notifications from the background or foreground.
* 🔒 **R8 Obfuscation & Resource Shrinking:**
  * Configured R8 code shrinking, resource shrinking, and ProGuard keep rules for optimized Google Play Store submission.

---

## 📐 Architecture & Project Structure

The application follows **Clean Architecture** principles combined with **MVVM**, Jetpack Compose, Material 3, and Kotlin **Coroutines** & **StateFlow**.

```text
com.danielculpian.cascadestudy
├── MainActivity.kt                      // Entry point, ViewModel factories, navigation, edge-to-edge
│
├── data
│   ├── local
│   │   ├── AppDatabase.kt               // Room database instance
│   │   ├── CompletedSessionDao.kt       // DAO queries for total, weekly, daily, and session flows
│   │   └── CompletedSessionEntity.kt    // Room entity for completed study records
│   └── repository
│       └── SessionRepository.kt         // Repository with weekly closed summary calculations
│
├── domain
│   ├── CascadeTimer.kt                  // Core timer engine & elapsed time logic
│   ├── StudySession.kt                  // Session configuration data class
│   ├── StudySessionPreset.kt            // Session preset enumeration
│   ├── StudySessionPresetExtensions.kt  // Preset to domain mapping
│   ├── SystemTimerClock.kt              // System clock implementation
│   ├── TimerClock.kt                    // Clock abstraction interface
│   ├── TimerEvent.kt                    // Sealed interface for state machine events
│   ├── TimerState.kt                    // Enum defining timer states
│   ├── TimerStateMachine.kt             // State machine transition rules
│   └── WeeklySummary.kt                 // Data class representing closed Monday-Sunday weeks
│
├── notification
│   └── NotificationHelper.kt            // Heads-up notification builder and channel manager
│
├── presentation
│   ├── AppScreen.kt                     // Screen navigation enumeration
│   ├── components
│   │   ├── AuthorCredits.kt             // Aesthetic GitHub author credit badge
│   │   └── StudyStatisticsCard.kt       // Reusable 3-stat card (Total, Weekly, Daily)
│   ├── history
│   │   ├── WeeklyHistoryScreen.kt       // Weekly history table UI with month/year/hours filters
│   │   ├── WeeklyHistoryUiState.kt      // UI state holder for weekly history
│   │   └── WeeklyHistoryViewModel.kt    // ViewModel managing history flows and filtering
│   ├── selection
│   │   └── SessionSelectionScreen.kt    // Main selection screen with info pop-up & history button
│   └── timer
│       ├── TimerScreen.kt               // Active timer screen with dynamic state badge colors
│       ├── TimerUiState.kt              // Immutable state data holder for timer UI
│       └── TimerViewModel.kt            // ViewModel coordinating state, alarms, sounds, and DB
│
├── receiver
│   ├── AlarmScheduler.kt                // Schedules exact kernel alarms via AlarmManager.setAlarmClock
│   └── TimerAlarmReceiver.kt            // BroadcastReceiver handling background interval transitions
│
├── sound
│   └── SoundManager.kt                  // MediaPlayer manager for audio effects
│
└── ui
    └── theme                            // Material 3 color schemes, typography, and theme
```

---

## 🔁 Navigation & Workflow

```text
  ┌─────────────────────────────────┐
  │     SessionSelectionScreen      │
  │  (Stats Card, Presets, Info,    │
  │   & Weekly History Button)      │
  └────────┬─────────────────┬──────┘
           │                 │ Click "Historial Semanal"
           │ Select Preset   ▼
           │        ┌─────────────────────────────────┐
           │        │       WeeklyHistoryScreen       │
           │        │ (Closed Weeks Table & Filters) │
           │        └─────────────────────────────────┘
           ▼
  ┌─────────────────────────────────┐
  │           TimerScreen           │
  │  (Countdown, Dynamic Colors,    │
  │   Stats, & Controls)            │
  └─────────────────────────────────┘
```

---

## 🧪 Testing Suite (44 Unit Tests)

The project includes a 100% passing automated test suite:

* **`CascadeTimerTest`**: Verifies interval transitions, countdown calculations, pause/resume time retention, and active interval elapsed time.
* **`StudySessionPresetTest` & `StudySessionTest`**: Validates preset structures and parameter bounds.
* **`TimerViewModelTest`**: Tests UI state flows, coroutine tickers, incremental DB persistence triggers, notification triggers, sound triggers, and alarm scheduling using `FakeCompletedSessionDao`, `FakeNotificationHelper`, `FakeSoundManager`, and `MainDispatcherRule`.
* **`WeeklyHistoryViewModelTest`**: Validates weekly summary filtering by month, year, and hour thresholds.
* **`SessionRepositoryTest`**: Verifies repository flow mappings, weekly start calculations, and session insertions.
* **`CompletedSessionDaoTest`**: Instrumented Room database tests on Android runtime.

---

## 🛠️ Tech Stack

| Component | Technology | Purpose |
| --- | --- | --- |
| **Language** | Kotlin (v2.4+) | Primary programming language |
| **UI Framework** | Jetpack Compose | Declarative UI |
| **Design System** | Material 3 | Adaptive styling, dynamic state colors, light/dark themes |
| **Architecture** | Clean Architecture + MVVM | Separation of concerns & state management |
| **Asynchrony** | Kotlin Coroutines & StateFlow | Reactive state updates & ticker timers |
| **Database** | Room (v2.8+) + KSP | Local SQLite persistence & reactive queries |
| **Background Alarms** | AlarmManager (`setAlarmClock`) | Exact 0% CPU background interval wakeups |
| **Audio & Media** | MediaPlayer | Custom audio effect playback |
| **Notifications** | NotificationManagerCompat | Heads-up system alerts |
| **Unit Testing** | JUnit 4 + Coroutines Test | 44 automated unit & integration tests |
| **Code Optimization** | R8 / ProGuard | Code obfuscation and resource shrinking for Google Play |

---

## 👤 Author & Credits

Designed and developed by **Daniel Culpian**.

* **GitHub:** [https://github.com/DanielCulpian](https://github.com/DanielCulpian)

---

## 📄 License

Cascade Study v2.0.1 — All rights reserved.
