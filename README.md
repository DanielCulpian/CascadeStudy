# Cascade Study

Cascade Study is an Android study-time management application based on the **cascade study technique**.

Instead of studying for fixed-length blocks, a session is divided into **progressively shorter study intervals** separated by short break periods. This makes long study sessions easier to maintain by gradually reducing the effort required as fatigue sets in.

---

## Session Presets

Cascade Study currently supports two main preset configurations:

### 1. Full Session (`FULL`)
- **Interval sequence:** `60 → 50 → 40 → 30 → 20 → 10` minutes
- **Rest duration:** `10` minutes between intervals
- **Total study time:** 210 minutes (3.5 hours)
- **Total rest time:** 50 minutes
- **Total duration:** 260 minutes (4h 20m)

### 2. Short Session (`SHORT`)
- **Interval sequence:** `30 → 20 → 10` minutes
- **Rest duration:** `10` minutes between intervals
- **Total study time:** 60 minutes (1 hour)
- **Total rest time:** 20 minutes
- **Total duration:** 80 minutes (1h 20m)

---

## Current Status

🚧 **Pre-beta / Active development**

The project includes a complete timer domain, multiple session presets, screen navigation, and full unit test coverage for domain rules and ViewModels.

### Current Functionality
- **Session Selection Screen:** Choose between preset session types (`FULL` or `SHORT`).
- **Timer Screen:** Responsive Material 3 UI displaying state, remaining time, and interval progress.
- **State Machine Transitions:**
  - Start session from the first interval.
  - Automatic transition from study to rest.
  - Automatic transition from rest to the next study interval.
  - Pause and resume while preserving remaining interval time.
  - Reset session back to idle.
  - Automatic detection of overall session completion.
- **Architecture & Quality:**
  - Clean separation of concerns (Domain, Presentation, UI).
  - Testable time abstraction (`TimerClock` / `FakeTimerClock`).
  - Unit tests covering domain logic, state machine, presets, and ViewModel state flows.

---

## Architecture

The application strictly separates **business domain logic** from **presentation and Compose UI**.

```text
com.example.cascadestudy
├── MainActivity.kt
│
├── domain
│   ├── CascadeTimer.kt
│   ├── StudySession.kt
│   ├── StudySessionPreset.kt
│   ├── StudySessionPresetExtensions.kt
│   ├── TimerClock.kt
│   ├── TimerEvent.kt
│   ├── TimerState.kt
│   └── TimerStateMachine.kt
│
├── presentation
│   ├── AppScreen.kt
│   ├── selection
│   │   └── SessionSelectionScreen.kt
│   └── timer
│       ├── TimerUiState.kt
│       ├── TimerViewModel.kt
│       └── TimerScreen.kt
│
└── ui
    └── theme
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

### Key Components

#### 1. Domain Layer (`domain/`)
- **`StudySession`**: Data class representing interval durations and rest time. Includes parameter validation.
- **`StudySessionPreset`**: Enum defining available presets (`FULL`, `SHORT`, `CUSTOM`).
- **`StudySessionPresetExtensions`**: Extension `StudySessionPreset.toStudySession()` mapping presets to domain session models.
- **`TimerState`**: Enum representing `IDLE`, `STUDYING`, `RESTING`, `PAUSED`, and `FINISHED`.
- **`TimerEvent`**: Sealed interface representing events that trigger state transitions.
- **`TimerStateMachine`**: State machine encapsulating valid state transitions.
- **`CascadeTimer`**: Core timer engine that coordinates session, state machine, and clock calculations using absolute timestamp comparison.
- **`TimerClock`**: Interface abstracting system time (`SystemTimerClock` for production, `FakeTimerClock` for tests).

#### 2. Presentation Layer (`presentation/`)
- **`AppScreen`**: Enum controlling app navigation (`SESSION_SELECTION`, `TIMER`).
- **`SessionSelectionScreen`**: Compose screen allowing the user to pick a session preset.
- **`TimerScreen`**: Compose screen rendering status, countdown, interval counts, and state controls.
- **`TimerViewModel`**: Manages UI state (`TimerUiState`), coordinates periodic ticker coroutines, and handles `selectPreset()`, `start()`, `pause()`, `resume()`, and `reset()`.

---

## App Flow

```text
  ┌───────────────────────────┐
  │  SessionSelectionScreen   │
  │  (AppScreen.SELECTION)    │
  └─────────────┬─────────────┘
                │ Select Preset (FULL / SHORT)
                ▼
  ┌───────────────────────────┐
  │        TimerScreen        │
  │    (AppScreen.TIMER)      │
  └───────────────────────────┘
```

---

## Testing

The project emphasizes unit testing domain logic and ViewModel state handling independently of Android framework dependencies.

### Covered Test Areas
- **`StudySessionTest`**: Validates input bounds (empty intervals, non-positive values).
- **`StudySessionPresetTest`**: Ensures `FULL` and `SHORT` presets map to correct interval configurations.
- **`CascadeTimerTest`**: Verifies state transitions, precise interval countdowns, pause/resume time preservation, and completion.
- **`TimerViewModelTest`**: Tests UI state updates, periodic ticker flows, reset actions, and preset selection updates.

---

## Tech Stack

| Technology | Purpose |
| --- | --- |
| **Kotlin** | Language |
| **Android Jetpack Compose** | Declarative UI |
| **Material 3** | Theme & Design System |
| **MVVM** | Architecture Pattern |
| **Kotlin Coroutines & StateFlow** | Reactive state management & asynchronous timer updates |
| **JUnit 4** | Unit testing framework |
| **Gradle (Kotlin DSL)** | Build system |

---

## License

License information will be added upon official publication.
