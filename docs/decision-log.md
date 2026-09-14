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

Following ADR-0010, `lintDebug` continued to report a further twelve warnings
concerning version currency: Kotlin, KSP, `androidx.core:core-ktx`, the three
`androidx.lifecycle` artefacts, `androidx.activity:activity-compose`, the
Jetpack Compose bill of materials (BOM), `androidx.test.ext:junit`, Espresso,
`kotlinx-coroutines-test`, the Gradle wrapper itself, and a `compileSdk` value
three levels below the ceiling that AGP 9.4 supports.

Every one of those was raised in the version catalogue: Kotlin to 2.4.20, KSP
to 2.3.12, core-ktx to 1.19.0, lifecycle to 2.11.0, activity-compose to
1.13.0, the Compose BOM to 2026.09.00, the AndroidX test extension to 1.3.0,
Espresso to 3.7.0, and coroutines-test to 1.11.0, together with the Gradle
wrapper to 9.7.1 and `compileSdk` and `targetSdk` to 37 in `app/build.gradle.kts`.
Room remained at 2.8.5, since lint did not flag it and Room 3.0 is a Kotlin
Multiplatform rewrite still in alpha with no stable release to move to. AGP
remained at 9.4.0, already the current release. Leaving Kotlin at 2.2.10 was
considered, on account of an open issue in the KSP issue tracker suggesting
that Kotlin 2.4.0 support was unfinished, but was rejected in favour of
testing the pairing directly rather than trusting the issue tracker: the
`kspDebugKotlin` and `compileDebugKotlin` tasks, together with Room's
annotation processing, all completed without error against KSP 2.3.12, which
already carries a fix for Kotlin 2.4.0's module naming. The
`-Xannotation-default-target=param-property` flag added under ADR-0010 was
removed in the same change, since Kotlin 2.4's default language version
already applies that behaviour and the compiler reported the flag itself as
redundant.

Consequence: `testDebugUnitTest`, `lintDebug` and `assembleDebug` all pass
with no dependency currency warnings remaining. Three `PluralsCandidate`
warnings remain in `strings.xml`, concerning strings that format a count
inline, which is a resource design question rather than a version problem
and is left open. The Kotlin and AGP bump also surfaced a further deprecation
warning, that `srcDirs` on the `androidTest` asset source set in
`app/build.gradle.kts` is deprecated, which stems from the same legacy
domain-specific language (DSL) compatibility mode as the two flags already
tracked under backlog item B11, and is recorded there rather than fixed with
an unconfirmed replacement.
