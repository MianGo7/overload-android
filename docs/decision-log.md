# Decision log

Architecture and tooling decisions in the order they were taken. Each entry
states the context, the decision, the alternatives that were rejected and the
reason for rejecting them, and the consequences that follow. When a decision
changes, a new entry is added rather than an existing one edited, so that the
reasoning at each point in time remains visible.

---

## ADR-0001, native Android with Kotlin. 2026-09-13, accepted.

The examination task requires Android activities, compliance with Material
Design and coverage by unit tests, which leaves little freedom in the choice of
platform. The project is therefore built natively for Android in Kotlin, using
Gradle and the Android Gradle plugin.

Flutter and React Native were considered and rejected. Either would produce a
mobile application, but neither produces activities in the sense in which the
task and the course material use the term, which would weaken the transfer
criterion of the assessment.

The consequence is a dependency on the Android software development kit and on
Android Studio, and the direct applicability of everything the course teaches
about activities, intents and the Android build.

---

## ADR-0002, several activities with Jetpack Compose inside each. 2026-09-13, accepted.

The task states that the application implements several Android activities.
Current Android practice favours a single activity hosting a navigation graph,
which contradicts that requirement directly. The project therefore uses one
activity per screen, navigates between them with explicit intents, and renders
each screen with Jetpack Compose and Material 3.

Navigation Compose within a single activity was rejected because it conflicts
with an explicit acceptance criterion. Extensible Markup Language (XML)
layouts with view binding were also rejected: they correspond more literally to
the course book, but they are slower to write and compliance with Material 3 is
more readily demonstrated in Compose.

The consequence is that navigation is explicit and straightforward to explain,
while no state is shared implicitly between screens, so each screen reads what
it requires from the repositories. The deviation from single activity practice
is argued in the report rather than concealed.

---

## ADR-0003, Room for persistence. 2026-09-13, accepted.

Entries and goals must survive application restarts and must be queryable per
week, so persistence is provided by Room on top of SQLite, with exported
schemas and flows for read access.

Storage in JavaScript Object Notation (JSON) files was rejected because it
would require hand written queries and offers no migration path. DataStore was
rejected because it suits key value settings rather than a list of records.

The consequence is one annotation processor in the build, and the obligation to
write and test a migration as soon as the schema changes.

---

## ADR-0004, a manual dependency container instead of a framework. 2026-09-13, accepted.

The dependency graph consists of one database, two repositories and four view
models. A hand written `AppContainer`, created by the application class and
read by the view model factories, is sufficient for a graph of that size.

Hilt was rejected. It is the industry default, but it introduces annotation
processing, generated components and lifecycle rules that would have to be
explained in the report without improving a graph this small.

The consequence is that the entire graph is visible in a single file. Should
the graph grow substantially, this entry is to be revisited rather than worked
around.

---

## ADR-0005, dependency versions pinned in a version catalogue. 2026-09-13, accepted.

A graded project must build reliably months after it was written, including on
an examiner's machine. All versions are therefore declared in
`gradle/libs.versions.toml` and the Gradle wrapper is committed.

Dynamic version declarations and unpinned plugin versions were rejected,
because they make a build reproducible only by accident.

The consequence is that upgrades are deliberate steps with their own commit.
The first such upgrade is recorded in ADR-0010.

---

## ADR-0006, indirect work counts as half a set. 2026-09-13, accepted.

A set of a compound exercise trains more than one muscle group. Counting that
set once for every muscle group involved inflates weekly volume, while counting
it only for the primary group understates it. A set therefore counts fully for
the muscle group the exercise trains directly and at half weight for each group
it trains indirectly, expressed as a single constant in `VolumeCalculator`.

Counting direct work alone was rejected as simpler but less representative of
how volume is programmed in practice. Per exercise weighting tables were
rejected because they would require a curated exercise database and more input
from the user than the scope of the project allows.

The consequence is that volume is fractional, so the interface formats values
such as 4.5 sets. The weighting is one constant, which can be discussed and
adjusted in the report without touching the remainder of the code.

---

## ADR-0007, the training week begins on Monday. 2026-09-13, accepted.

Weekly volume is meaningful only against a fixed window. A training week is
therefore an International Organization for Standardization (ISO) week
beginning on Monday, modelled by `TrainingWeek`, which validates its own start
date.

A rolling window of the previous seven days was rejected because it never
assigns a week a final value, which makes the history meaningless. A
configurable week start was rejected as a setting that adds no insight.

The consequence is that an entry logged late on a Sunday belongs to the week
that is ending, and that a user training on a different cycle sees volume split
across two weeks. This is a limitation worth naming in the critical evaluation.

---

## ADR-0008, training data remains on the device. 2026-09-13, accepted.

The database is excluded from cloud backup and included in a direct device to
device transfer, as declared in `res/xml/data_extraction_rules.xml`.

The default behaviour was rejected, because it would copy personal training
data to a cloud backup without the user being asked.

The consequence is that a user who restores from a cloud backup begins with an
empty log. This is a deliberate trade off between convenience and privacy, and
a concrete example of a non functional decision for the report.

---

## ADR-0009, aggregation in Kotlin rather than in SQL. 2026-09-13, accepted.

Repositories return rows and the domain layer aggregates them. Aggregation in a
grouped SQL query was rejected because it would be faster on a long history but
would move a rule into a query string, where it cannot be unit tested on the
Java Virtual Machine.

The consequence is that the rules remain testable without a device, at the cost
of reading the history into memory. For a single user logging a few entries per
day this amounts to kilobytes. The decision is to be revisited if the history
reaches tens of thousands of rows.

---

## ADR-0010, moving to AGP 9 rather than pinning back. 2026-09-14, accepted.

Opening the project in Android Studio for the first time triggered the Android
Gradle plugin (AGP) upgrade assistant, which raised Gradle to 9.6.0, AGP to
9.4.0, Kotlin to 2.2.10 and Kotlin Symbol Processing (KSP) to 2.3.6, while
leaving Room at 2.6.1. The build then failed in the `kspDebugKotlin` task with
`unexpected jvm signature V`, a known error produced when KSP2 processes an
annotation processor that predates it. Room has supported KSP2 since version
2.7.0.

Two courses of action were available. Pinning Gradle, AGP, Kotlin and KSP back
to the versions recorded in ADR-0005 was rejected, because it would require
declining the upgrade prompt for the remainder of the project and submitting
work built on a toolchain more than a year old, whereas the alternative
amounted to a single version change. Room was therefore raised to 2.8.5, the
current stable release.

The principle established in ADR-0005 is unaffected: versions remain pinned in
the catalogue and upgrades remain deliberate. The upgrade assistant also added
a set of compatibility flags to `gradle.properties`. Those not needed by this
project were removed, and the two that remain are tracked as backlog item B11,
since both are removed in AGP 10.

---

## ADR-0011, every pinned version raised to its current stable release. 2026-09-14, accepted.

Following ADR-0010, `lintDebug` reported twelve more version currency
warnings: Kotlin, KSP, the AndroidX core, lifecycle, activity and test
libraries, the Compose bill of materials (BOM), coroutines-test, the Gradle
wrapper, and `compileSdk` below the ceiling AGP 9.4 supports. Every one was
raised to its current release: Kotlin 2.4.20, KSP 2.3.12, the Compose BOM
2026.09.00, `compileSdk`/`targetSdk` 37, and the rest of the listed libraries
and the Gradle wrapper to their latest versions. Room and AGP stayed put,
neither was flagged and Room 3.0 is still alpha.

Leaving Kotlin at 2.2.10 was considered, since an open KSP issue suggested
2.4.0 support was unfinished, but rejected in favour of testing the pairing
directly: `kspDebugKotlin` and Room's annotation processing both completed
without error against KSP 2.3.12. The `-Xannotation-default-target` flag from
ADR-0010 was dropped in the same change, redundant now that Kotlin 2.4's
default language version already applies that behaviour.

Consequence: all three Gradle commands pass with no dependency currency
warnings left. The version bump also surfaced a `srcDirs` deprecation,
tracked under B11 since it shares that item's root cause.

---

## ADR-0012, drop the standalone Kotlin Gradle plugin for AGP's built-in Kotlin support. 2026-09-14, accepted.

Running with `-Pandroid.debug.obsoleteApi=true`, as B11 prescribed, traced
both remaining `gradle.properties` compatibility flags to one cause: AGP's
own reported reason for every `applicationVariants`/`testVariants` warning
was that the `org.jetbrains.kotlin.android` plugin itself calls that legacy
API, not this project's build script. `android.newDsl` and
`android.builtInKotlin` were therefore one migration, not two, solved by
dropping that plugin in favour of AGP 9's built-in Kotlin support.

The plugin alias was removed from both `build.gradle.kts` files and both
flags from `gradle.properties`. KSP, the Compose compiler plugin and the
`kotlin { compilerOptions { } }` block all kept working, and the `srcDirs`
deprecation from ADR-0011 disappeared with them, confirming the shared cause.
A further, previously unnoticed deprecation,
`android.dependency.excludeLibraryComponentsFromConstraints`, was fixed the
same way AGP's own warning suggested. The configuration cache, B11's last
item, was enabled and verified with two clean builds, one to write it and
one to read it back.

Rejected: porting a build script caller to `androidComponents { onVariants
{ } }` as B11 originally assumed might be needed. Investigating first showed
no such caller existed in this project's own code.

Consequence: `gradle.properties` holds no AGP compatibility flags, all three
Gradle commands run with no deprecation warnings, and B11 is closed.

---

## ADR-0013, separate dark theme colours for the volume status indicators. 2026-09-14, accepted.

`VolumeStatus.color()` used one fixed colour per status in both themes.
Computing the WCAG contrast ratio of each against the dark background,
Graphite, rather than checking by eye, gave 3.54:1, 3.33:1 and 2.42:1, all
below the 4.5:1 threshold for normal text. `BelowTarget` also only reached
4.49:1 against the light background. `UNTARGETED` had no named colour at
all, it used the raw `Color.Gray` constant directly.

Every status now has a light and a brighter dark value in `ui/theme/Color.kt`
clearing 4.5:1 with a comfortable margin, `BelowTarget` was darkened slightly
so its light value clears it too, and `Untargeted`/`UntargetedDark` were
added so every status is a named, checked colour rather than a literal.
`VolumeStatus.color()` became a composable that reads
`isSystemInDarkTheme()` to pick between the two sets.

Rejected: relying on Material 3's dynamic colour scheme to compensate.
Dynamic colour only affects roles Material 3 itself manages, not custom
semantic colours declared outside that scheme.

Consequence: every status colour clears WCAG AA 4.5:1 in both themes.

---

## ADR-0014, WorkManager for the logging reminder, with a real schema migration. 2026-09-14, accepted.

B4's optional daily reminder needs to survive process death and reboots, so
`androidx.work:work-runtime-ktx` 2.11.2 was added, a new dependency. A
`PeriodicWorkRequest` with a computed initial delay is used rather than
`AlarmManager`, since WorkManager already handles doze mode and reboot
persistence and the task description asks for it by name; the trade off is
that the fire time is approximate, WorkManager batches work for battery
reasons rather than guaranteeing the exact minute.

Storing the chosen time required a real schema change, the first one this
project has made: `TrainingGoalEntity` gained a nullable `reminder_time`
column, minute of day as an integer, null meaning no reminder is set.
`AppDatabase` moved to version 2 with `MIGRATION_1_2` adding the column,
never a destructive migration, and the exported schema plus a
`MigrationTestHelper` test cover it. This also satisfies backlog item B7,
which existed to require exactly this the first time the schema changed.

Rejected: `AlarmManager` with exact alarms, which needs the
`SCHEDULE_EXACT_ALARM` permission and Android 12's stricter exact alarm
rules for no benefit a training reminder needs, being a minute or two late
does not matter.

Consequence: a new dependency and the project's first Room migration, both
exercised by tests rather than only by the schema export.

Two problems surfaced only by testing on device rather than trusting the
code. First, `room-testing` needs a newer `kotlinx-serialization` than
`androidx.savedstate` strictly requires, and `work-runtime` bundles its own
Room usage which strictly pins an older `kotlinx-coroutines-core` than
`kotlinx-coroutines-test` needs; both were pinned higher with
`resolutionStrategy.force` in `app/build.gradle.kts`, a plain constraint
cannot outrank a strictly one. Second, rescheduling to a new time silently
kept the old schedule: `ExistingPeriodicWorkPolicy.UPDATE` preserves an
existing periodic work's anchor rather than a new `setInitialDelay`, found by
setting a reminder a minute out and watching it fire almost a day late.
`CANCEL_AND_REENQUEUE` replaces the schedule outright, which is what picking
a new time is meant to do.

---

## ADR-0015, Compose Canvas for the volume trend chart, no charting library. 2026-09-14, accepted.

B5 needed a chart of weekly volume against the target band. Drawn directly
with Compose `Canvas` primitives, `drawLine`, `drawRect` and `drawCircle`,
rather than adding a charting dependency, since the shape needed, a line
with a band behind it over a fixed twelve week window, does not warrant the
weight or the licence and maintenance surface of a library, and the task
description names `Canvas` as the preferred approach. The chart carries no
semantics of its own, `clearAndSetSemantics {}` marks it decorative, a plain
text summary of the current week against the band sits beside it for screen
readers, following the same pattern ADR-0013 used for accessible custom
drawing.

Rejected: a charting dependency such as Vico or MPAndroidChart, which would
need its own ADR under the dependency rule and brings far more than a single
line and band chart requires.

Consequence: the chart is a small, fully owned composable with no external
API to track for breaking changes, at the cost of writing the axis and
scaling by hand.
