# Backlog

Engineering work items, roughly in the order they should be taken. Each item
is sized to fit one working session and to produce a reviewable diff.

How to use this file:

- Take one item at a time. Read the item, read the files it names, settle on a
  plan, implement it, run the checks, update the documents listed under
  "documents to update", then commit.
- Do not start a second item before the first one is committed.
- If an item turns out to require a decision that is not already settled in
  `decision-log.md`, settle it deliberately and record an ADR for it.
- Status values: open, in progress, done. Keep them current.

Every item inherits the repository conventions: rules live in the domain layer
and are unit tested, nothing is pushed automatically, and the definition of
done at the end of this file applies.

---

## B0, get the first build green

Status: open. Priority: blocking, nothing else can start.

**Goal.** The project compiles, the unit tests run and the debug build
installs.

**Context.** The project has not been compiled yet. Expect version
mismatches, a wrong import or an API that moved between Compose releases.

**Scope.**

- Fix compilation and sync errors until `./gradlew testDebugUnitTest`,
  `./gradlew lintDebug` and `./gradlew assembleDebug` all succeed.
- If a dependency version has to change, change it in
  `gradle/libs.versions.toml` and nowhere else, and add a line to ADR-0005
  saying which version moved and why.
- Do not fix a failure by deleting a test, weakening an assertion or
  flattening the layering. Those are the parts being graded.

**Done when.** The three Gradle commands above pass, the app launches on a
device or emulator, and the dev journal records what had to be changed.

**Documents to update.** `dev-journal.md`, and ADR-0005 if versions moved.

---

## B1, unit tests for the form validation rules

Status: open. Priority: high. Grade link: quality, criterion AC6.

**Goal.** The validation rules in the two form view models are covered by JVM
unit tests.

**Scope.**

- `GoalViewModel`: a row with both fields empty is skipped, a row with only
  one field filled is rejected, a minimum above the maximum is rejected, valid
  rows are turned into targets, the block length is clamped to the allowed
  range.
- `LogEntryViewModel`: blank exercise name, zero or missing sets or reps and a
  missing weight are rejected, a comma is accepted as a decimal separator,
  selecting a muscle group as primary removes it from the secondary set.
- If testing a rule requires an Android class, extract the rule into a pure
  function in `domain` or a plain Kotlin class in the same ui package and test
  that instead. Do not add Robolectric.
- Use fake repositories, not Room. A small in memory fake that implements the
  repository interface belongs in `app/src/test/java/.../fake/`.

**Done when.** New tests pass, the rules above are each covered by a named
test, and AC6 in `requirements.md` moves from partly to done.

**Documents to update.** `requirements.md`, `dev-journal.md`.

---

## B2, seed data for demonstrations and screenshots

Status: open. Priority: high, unblocks anything visual. Grade link:
documentation, creativity.

**Goal.** A debug only action that fills the database with a plausible
training history.

**Scope.**

- Six to eight weeks of entries across all tracked muscle groups, with a goal
  configured, realistic exercise names, loads and rep ranges.
- One of those weeks must be a genuine deload week, meaning its volume is at
  or below half of the trailing average, so that the deload advice can be
  demonstrated and screenshotted.
- Debug builds only. Use a `debug` source set or a `BuildConfig.DEBUG` guard
  so that nothing ships in a release build.
- Trigger it from an obvious place in debug, for example an action in the
  dashboard top bar that only exists in debug.

**Done when.** A fresh install plus one tap produces a populated dashboard, a
populated history and a visible deload state, and no seeding code is reachable
in a release build.

**Documents to update.** `dev-journal.md`.

---

## B3, Material 3 and accessibility pass

Status: open. Priority: high. Grade link: quality, criterion AC7.

**Goal.** The app visibly follows Material Design and the Android app quality
guidelines rather than merely using Material components.

**Scope.**

- Every interactive element has a minimum touch target of 48 dp and, where it
  is not self explanatory, a content description.
- Check light theme, dark theme and dynamic colour on Android 12 and above.
  The status colours in `ui/theme/Color.kt` must stay readable in both themes,
  adjust them if they do not.
- Check text scaling at 200 percent and landscape orientation. Nothing may be
  cut off or overlap.
- No hardcoded user facing strings anywhere, plurals use plural resources.
- Empty states, loading states and error states exist on every screen.
- Run TalkBack over the main flow once and fix what is unusable.
- Fix every accessibility and usability warning Android lint reports.

**Done when.** `./gradlew lintDebug` reports no accessibility warnings, the
checks above are recorded as a short checklist in the journal entry, and AC7
in `requirements.md` moves to done.

**Documents to update.** `requirements.md`, `dev-journal.md`.

---

## B4, logging reminder with a notification

Status: open. Priority: high. Grade link: transfer, this is the item that maps
most directly onto the course material.

**Goal.** An optional daily reminder to log training, which the user can
enable and time on the goal screen.

**Scope.**

- Schedule with WorkManager. Add the dependency to the version catalogue and
  record an ADR for it.
- A `BroadcastReceiver` for `BOOT_COMPLETED` so that the schedule survives a
  restart. This is the part that demonstrates unit 5 of the course book.
- The notification opens `LogEntryActivity` through an explicit intent.
- Handle the `POST_NOTIFICATIONS` runtime permission on API 33 and above,
  including the case where the user declines.
- Skip the reminder on a day where something has already been logged.
- The reminder setting is stored with the goal, not in a separate mechanism.

**Done when.** The reminder fires, survives a reboot, opens the entry screen,
behaves correctly when the permission is denied, and the new dependency has
an ADR.

**Documents to update.** `decision-log.md`, `requirements.md`,
`dev-journal.md`.

---

## B5, weekly volume trend

Status: open. Priority: medium. Grade link: creativity, and it strengthens the
critical evaluation chapter.

**Goal.** A chart showing accumulated weekly sets over the last eight to
twelve weeks, so that trends and deloads are visible at a glance.

**Scope.**

- Overall volume, and per muscle group when one is selected.
- Draw the target range as a band behind the line so that the chart answers
  the same question as the dashboard.
- Prefer Compose `Canvas` over a charting dependency. If a dependency is
  genuinely better, propose it first and record an ADR.
- The chart needs a text alternative for accessibility, for example a summary
  line stating the trend.
- Colours must work in light and dark themes.

**Done when.** The history screen shows the trend, it renders correctly with
one week of data and with twelve, and it is readable in both themes.

**Documents to update.** `decision-log.md` if a dependency is added,
`dev-journal.md`.

---

## B6, UI test for the main flow

Status: open. Priority: medium. Grade link: quality, depth of AC6.

**Goal.** One instrumented Compose test covering the path a user actually
takes.

**Scope.**

- Set a goal, log an entry, assert that the dashboard shows the new volume and
  the correct status for that muscle group.
- Use an in memory Room database and the real view models. Do not assert on
  padding, colours or layout details.

**Done when.** `./gradlew connectedDebugAndroidTest` passes on a device or
emulator.

**Documents to update.** `requirements.md`, `dev-journal.md`.

---

## B7, Room migration and its test

Status: open. Priority: medium, becomes urgent the moment the schema changes.

**Goal.** The first schema change ships with a real migration instead of a
destructive one.

**Scope.**

- Bump the database version, write the `Migration` object, commit the exported
  schema JSON under `app/schemas`.
- Add a migration test using `MigrationTestHelper` that opens the old schema,
  inserts a row, migrates and asserts the row survived.
- Never enable `fallbackToDestructiveMigration`.

**Done when.** The migration test passes and the exported schemas for both
versions are committed.

**Documents to update.** `dev-journal.md`.

---

## B8, source code documentation pass

Status: open. Priority: medium. Grade link: criterion AC8.

**Goal.** The documentation carries the reasoning, not a restatement of the
code.

**Scope.**

- KDoc on every public type and every non obvious public function, explaining
  why it exists and what invariants it keeps.
- Every constant that encodes a domain decision, for example the half set
  credit and the deload ratio, names the decision and points at the ADR.
- Remove comments that only repeat the line below them.

**Done when.** A reviewer can read the domain layer top to bottom and
understand the rules without opening the report.

**Documents to update.** `requirements.md`, `dev-journal.md`.

---

## B9, edge cases

Status: open. Priority: low, but each one is a sentence in the evaluation
chapter.

**Scope.**

- The app is open when the week rolls over at midnight. The dashboard should
  move to the new week rather than keep showing the old one.
- Entries dated in the future, and entries dated far in the past.
- Locale decimal separators in the weight field.
- Very large inputs, for example 999 sets, and how the progress bar behaves.
- Deleting an entry, which the repository supports but no screen exposes yet.

**Done when.** Each case is either handled with a test, or listed in the
journal as a known limitation with a reason. A named limitation is worth more
in the report than a silent bug.

**Documents to update.** `dev-journal.md`.

---

## B10, export the log as CSV

Status: open. Priority: optional.

**Goal.** Share or save the training log as a CSV file.

**Scope.** Build the file in the cache directory, hand it out with a
`FileProvider` and an implicit share intent. This is a second, clean example
of intents for the transfer chapter.

**Done when.** The file opens correctly in a spreadsheet application and the
share sheet appears on a real device.

**Documents to update.** `dev-journal.md`.

---

## Definition of done

An item is done when:

- `./gradlew testDebugUnitTest` and `./gradlew lintDebug` pass,
- the documents listed by the item are updated in the same commit,
- the item status above is current,
- `requirements.md` still reflects reality,
- and the change is committed locally. Pushing is a manual step.
