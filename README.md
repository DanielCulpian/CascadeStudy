# Cascade Study

Cascade Study is a study-time management application based on a **cascade study technique**.

The idea is simple: instead of studying for one fixed period, a session is divided into progressively shorter study intervals separated by short breaks.

The current default session is:

**60 → 50 → 40 → 30 → 20 → 10 minutes**

with a **10-minute break between each study interval**.

This results in:

- 210 minutes of total study time
- 50 minutes of total break time
- 260 minutes (4h 20m) for the complete session

The project is currently under development as an Android application.

---

## Current Status

🚧 **Pre-beta / Active development**

The core timer domain is already implemented and covered by unit tests. The Compose UI is currently being developed and refined.

Current functionality includes:

- Starting a study session
- Countdown timer
- Automatic transition from study to rest
- Automatic transition from rest to the next study interval
- Pausing and resuming
- Resetting a session
- Detecting the end of the complete session
- Displaying the current study interval
- Material 3 based UI
- Automatic update of the UI while the timer is running
- Unit tests for the timer domain and ViewModel

Future versions will focus on configuration, persistence, notifications/audio feedback and a more complete user experience.

---

## The Cascade Technique

A standard session consists of progressively shorter study intervals:

Study   60 min
Rest    10 min

Study   50 min
Rest    10 min

Study   40 min
Rest    10 min

Study   30 min
Rest    10 min

Study   20 min
Rest    10 min

Study   10 min

The final study interval is not followed by a break.

The technique is intended to make long study sessions more manageable by gradually reducing the length of each subsequent study period.

The session configuration is represented by the StudySession domain model, allowing different interval configurations to be supported in the future.


---

Architecture

The Android application follows a separation between domain logic and presentation logic.

The current architecture is based on:

Kotlin

Jetpack Compose

MVVM

Material 3

Kotlin Coroutines

StateFlow

JUnit


The project is organized around the following structure:

com.example.cascadestudy
├── MainActivity.kt
│
├── domain
│   ├── CascadeTimer.kt
│   ├── StudySession.kt
│   ├── TimerClock.kt
│   ├── TimerEvent.kt
│   ├── TimerState.kt
│   └── TimerStateMachine.kt
│
├── presentation
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

Domain

The domain layer contains the timer's business rules and does not depend on the Compose UI.

StudySession

Defines the configuration of a study session:

StudySession(
    intervals = listOf(60, 50, 40, 30, 20, 10),
    restDurationMinutes = 10
)

It also validates the session configuration.

TimerState

Represents the possible states of the timer:

IDLE
STUDYING
RESTING
PAUSED
FINISHED

TimerEvent

Represents events that can affect the timer state:

Start
Pause
Resume
Reset
IntervalFinished
RestFinished

TimerStateMachine

Contains the rules for valid state transitions.

For example:

IDLE
  ↓ Start
STUDYING
  ↓ IntervalFinished
RESTING
  ↓ RestFinished
STUDYING

The state machine also handles:

STUDYING ──→ PAUSED ──→ STUDYING
RESTING  ──→ PAUSED ──→ RESTING

and eventually:

STUDYING
   ↓
FINISHED

CascadeTimer

Acts as the timer controller.

It coordinates:

StudySession

TimerStateMachine

TimerClock


It is responsible for:

Tracking the current interval

Calculating the remaining time

Starting and ending intervals

Handling pauses and resumes

Advancing through the cascade

Detecting when the session has finished


The timer uses an absolute end time internally rather than simply decrementing a counter every second.


---

Time Abstraction

The timer uses a TimerClock abstraction:

interface TimerClock {
    fun nowMillis(): Long
}

The production implementation provides the real system time, while tests use a fake clock.

This allows timer behavior to be tested deterministically without having to actually wait for minutes or hours.

For example:

val fakeClock = FakeTimerClock()

fakeClock.advanceMillis(20_000L)

timer.update()

This approach is especially important for testing pause/resume behavior and interval transitions.


---

Presentation Layer

The presentation layer currently follows an MVVM-style structure.

TimerViewModel

The ViewModel owns the UI state and coordinates the timer with the Compose UI.

The UI observes:

StateFlow<TimerUiState>

The ViewModel exposes operations such as:

start()
pause()
resume()
reset()

The UI does not directly implement timer business logic.

TimerUiState

The UI receives a simplified representation of the timer:

data class TimerUiState(
    val state: TimerState,
    val remainingSeconds: Long,
    val currentIntervalIndex: Int,
    val totalIntervals: Int
)

This keeps the Compose layer focused on presentation rather than domain calculations.


---

UI

The application uses Jetpack Compose and Material 3.

The current timer screen displays:

Current timer state

Countdown

Current interval

Total number of intervals

Contextual controls for starting, pausing, resuming and resetting


The visual design is still being developed.

The application is being structured around Material 3's ColorScheme, with the intention of supporting:

Light theme

Dark theme

Automatic theme selection based on the device configuration


The goal is to keep the UI independent from individual hard-coded colors so that both themes can share the same UI components.


---

Testing

Testing is an important part of the project.

The timer domain is designed to be testable independently from Android and the Compose UI.

Current tests cover areas such as:

StudySession

Empty intervals are rejected

Non-positive intervals are rejected

Invalid rest durations are rejected


CascadeTimer

Starting a session

Study → rest transitions

Rest → next study interval transitions

Completing the final interval

Pausing

Resuming

Preserving remaining time

Resetting

Single-interval sessions

Remaining-time rounding


TimerViewModel

UI state transitions

Automatic timer updates

Pause/resume behavior

Reset behavior

Automatic session completion

Invalid operations

Number of configured intervals


A fake clock and coroutine test dispatcher are used to keep tests fast and deterministic.


---

Development Philosophy

The project is intentionally being developed incrementally.

The main goals are:

1. Keep business logic independent from the UI.


2. Make state transitions explicit.


3. Make time-based behavior testable.


4. Avoid putting business logic inside Compose components.


5. Keep the code simple and maintainable.


6. Build the application in small, testable steps.



The project is not intended to be just a countdown timer. The long-term goal is to provide a flexible study-session management tool built around the cascade technique.


---

Roadmap

Planned features and improvements include:

Timer

[x] Cascade study intervals

[x] Rest intervals

[x] Pause / resume

[x] Reset

[x] Automatic interval transitions

[x] Session completion

[x] Deterministic timer tests

[ ] Improved timer lifecycle handling

[ ] Monotonic time source for Android


UI

[x] Jetpack Compose

[x] Material 3

[x] Timer screen

[x] Basic state-based controls

[ ] Refined visual design

[ ] Light theme

[ ] Dark theme

[ ] Automatic system theme support

[ ] Progress visualization


Session Configuration

[ ] Custom study intervals

[ ] Custom rest duration

[ ] Preset session lengths

[ ] Shorter cascade sessions


For example:

30 → 20 → 10

Notifications & Feedback

[ ] Audio notification when an interval ends

[ ] Haptic feedback

[ ] Android notifications

[ ] Background timer support


Persistence

[ ] Save user preferences

[ ] Save custom session configurations

[ ] Session history

[ ] Study statistics


Future Platforms

The original concept was first prototyped in Python before the Android application was started.

The original prototype used Tkinter and is intended to remain a useful reference for the core concept.

A future desktop version may provide a dedicated desktop implementation.


---

Project Origins

The first version of Cascade Study was created as a small Python prototype to validate the cascade study concept.

The prototype focused on the basic timing sequence before moving to the Android implementation.

The Android version is being developed with a stronger focus on:

Architecture

Testability

Separation of concerns

Maintainability

Extensibility


The Android application is therefore not simply a direct port of the original prototype.


---

Tech Stack

Technology	Purpose

Kotlin	Main programming language
Android	Target platform
Jetpack Compose	UI
Material 3	Design system
MVVM	Presentation architecture
Kotlin Coroutines	Asynchronous timer updates
StateFlow	Reactive UI state
JUnit	Unit testing
Gradle Kotlin DSL	Build configuration



---

License

License information will be added when the project reaches the appropriate stage for publication.
