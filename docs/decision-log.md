# Decision log

Architecture and tooling decisions, newest last. Each entry states the
context, the decision, the alternatives that were rejected and the
consequences. When a decision changes, add a new entry that supersedes the
old one instead of editing the old one.

Format: ADR-NNNN, title, date, status.

---

## ADR-0001, native Android with Kotlin, 2026-09-13, accepted

**Context.** The examination task requires Android activities, Material Design
compliance, unit tests and a buildable Android project.

**Decision.** Native Android with Kotlin, built with Gradle and the Android
Gradle Plugin.

**Rejected.** Flutter and React Native. Both would satisfy "a mobile app" but
neither produces Android activities in the sense the task and the course
material use the term, which would weaken the transfer criterion of the grade.

**Consequences.** The project depends on the Android SDK and Android Studio.
Everything the course teaches about activities, intents and the Android build
applies directly.

---

## ADR-0002, several activities with Jetpack Compose inside each, 2026-09-13, accepted

**Context.** The task requires that the app "implements several Android
activities". Current Android practice is a single activity with a navigation
graph, which directly contradicts that wording.

**Decision.** One activity per screen, navigation between them with explicit
intents, and Jetpack Compose with Material 3 as the UI toolkit inside each
activity.

**Rejected.** Single activity with Navigation Compose, which conflicts with
the acceptance criterion. XML layouts with view binding, which would match the
course book more literally but is dated and slower to write, and Material 3
compliance is easier to demonstrate with Compose.

**Consequences.** Navigation is explicit and easy to explain in the report.
State is not shared automatically between screens, so each screen loads what
it needs from the repositories. The deviation from single activity practice
must be justified in the report rather than hidden.

---

## ADR-0003, Room for persistence, 2026-09-13, accepted

**Context.** Entries and goals must survive app restarts and must be queried
per week.

**Decision.** Room on top of SQLite, with exported schemas and flows for
reads.

**Rejected.** JSON files, which would need hand written queries and offer no
migration story. DataStore, which fits key value settings rather than a list
of records.

**Consequences.** One annotation processor, KSP, in the build. Schema
migrations must be written and tested once the schema changes.

---

## ADR-0004, manual dependency container instead of a DI framework, 2026-09-13, accepted

**Context.** The app has one database, two repositories and four view models.

**Decision.** A hand written `AppContainer` created by the `Application` and
read by view model factories.

**Rejected.** Hilt. It is the industry default, but it adds annotation
processing, generated components and lifecycle rules that would have to be
explained in the report without improving a graph this small.

**Consequences.** Wiring is visible in one file and easy to explain. If the
graph grows substantially, revisit this entry rather than bolting on
workarounds.

---

## ADR-0005, pinned dependency versions in a version catalogue, 2026-09-13, accepted

**Context.** A graded project must build reliably months after it was written,
including on the examiner's machine.

**Decision.** All versions live in `gradle/libs.versions.toml` and are pinned
to known compatible releases. The Gradle wrapper is committed.

**Rejected.** Dynamic versions and unpinned plugin versions, which make a
build reproducible only by accident.

**Consequences.** Upgrades are deliberate steps with their own commit. Android
Studio may suggest newer versions, which should be applied through the AGP
upgrade assistant and recorded here.

---

## ADR-0006, indirect work counts as half a set, 2026-09-13, accepted

**Context.** A set of a compound exercise trains more than one muscle group.
Counting it once per involved group inflates weekly volume, counting only the
primary group underreports it.

**Decision.** A set counts fully for the muscle group the exercise trains
directly and half for each group it trains indirectly. The factor is a
constant in `VolumeCalculator`.

**Rejected.** Counting only direct work, which is simpler but does not reflect
how volume is usually programmed. Per exercise weighting tables, which would
need a curated exercise database and more input from the user than the scope
allows.

**Consequences.** Volume is fractional, so the UI formats values such as 4.5
sets. The factor is a single constant and can be discussed and changed in the
report without touching the rest of the code.

---

## ADR-0007, the training week starts on Monday, 2026-09-13, accepted

**Context.** Weekly volume only makes sense against a fixed window.

**Decision.** A training week is an ISO week starting on Monday, modelled by
`TrainingWeek`, which validates its own start date.

**Rejected.** A rolling window of the last seven days, which never gives a
week a final value and makes history meaningless. A user configurable week
start, which adds a setting without adding insight.

**Consequences.** Entries logged late on a Sunday still belong to the week
that is ending. Users who train on a different cycle see split volume, which
is a known limitation worth naming in the critical evaluation.

---

## ADR-0008, training data stays on the device, 2026-09-13, accepted

**Context.** Android backup rules decide whether app data is copied to a cloud
backup.

**Decision.** The database is excluded from cloud backup and included in a
direct device to device transfer, see `res/xml/data_extraction_rules.xml`.

**Rejected.** Default behaviour, which would upload personal training data to
a cloud backup without the user ever being asked.

**Consequences.** A user who restores from a cloud backup starts with an empty
log. This is a deliberate privacy tradeoff and a concrete example of a
non functional decision for the report.

---

## ADR-0009, aggregation happens in Kotlin, not in SQL, 2026-09-13, accepted

**Context.** Weekly totals could be computed with a grouped SQL query or in
the domain layer after reading the rows.

**Decision.** Repositories return rows, the domain layer aggregates them.

**Rejected.** SQL aggregation, which would be faster for large histories but
would move a rule into a query string where it cannot be unit tested on the
JVM.

**Consequences.** The rules stay testable without an Android device, at the
cost of reading a full history into memory. For a single user logging a few
entries per day this is measured in kilobytes. Revisit if the history grows
into tens of thousands of rows.
