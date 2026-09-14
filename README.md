# Overload

An Android app for tracking weekly training volume, meaning hard sets per
muscle group, against a target range the user sets for themselves. The app
shows which muscle groups are still short of their weekly range and flags when
an accumulation block has run long enough that a deload week is due.

This repository is the practical part of a project report for the IU course
DLBCSEMSE02, Mobile Software Engineering II.

- Course: DLBCSEMSE02, Mobile Software Engineering II
- Task: task 1, individual tracking app
- Author: Mian Gohar Ehsan
- Matriculation number: IU14147184
- Tutor: Christian Remfert

## What the app does

- The user defines a weekly set range per muscle group together with a
  mesocycle phase and a block length.
- Sets are logged per exercise with date, repetitions, load and optional reps
  in reserve, including the muscle groups an exercise trains indirectly.
- A set counts fully for the muscle group it trains directly and half for each
  indirectly trained group.
- The dashboard evaluates the current week and shows, per muscle group,
  whether the accumulated volume is below, inside or above the target range.
- The history lists past weeks, marks deload weeks and opens a detailed
  breakdown of any week.

## Requirements

- Android Studio, recent stable release
- JDK 17, bundled with Android Studio
- Android SDK 35
- A device or emulator running Android 8.0, API 26, or newer

## Build and run

    git clone <repository url>
    cd overload-android
    ./gradlew assembleDebug

Then open the folder in Android Studio and run the `app` configuration, or
install directly:

    ./gradlew installDebug

## Tests

    ./gradlew testDebugUnitTest     # domain rules, runs on the JVM
    ./gradlew connectedDebugAndroidTest   # Room queries, needs a device
    ./gradlew lintDebug             # Android lint

The domain layer contains no Android dependency, so every rule about counting
volume, evaluating progress and detecting deloads is tested without an
emulator.

## Project structure

    app/src/main/java/de/miangohar/overload/
      OverloadApplication.kt   application entry point, owns the container
      di/                      manual dependency wiring
      domain/model             data classes with validation
      domain/logic             volume counting, evaluation, deload advice
      domain/repository        repository interfaces
      data/local               Room database, entities, DAOs, converters
      data/repository          Room backed repository implementations
      ui/dashboard             current week overview
      ui/goal                  weekly goal configuration
      ui/logentry              entry form
      ui/history               past weeks and week detail
      ui/common, ui/theme      shared composables, Material 3 theme

    docs/
      concept.md               the concept the task asks for
      requirements.md          acceptance criteria and their status
      decision-log.md          architecture decisions with alternatives
      dev-journal.md           chronological development record
      backlog.md               scoped work items, taken one at a time
      wireframes/              screen sketches

One further file, `docs/course-context.local.md`, holds a paraphrase of the
examination requirements. It is intentionally not committed, because IU holds
the copyright on its examination tasks and objects to them being published on
third party platforms.

## Architecture

The dependency direction is one way, `ui` and `data` both depend on `domain`
and never the other way around. Rules live in `domain/logic`, view models hold
state and delegate, Room implements the repository interfaces declared in the
domain layer.

The app deliberately uses one activity per screen with explicit intents rather
than a single activity with a navigation graph. The reason is recorded in
`docs/decision-log.md`, entry ADR-0002.
