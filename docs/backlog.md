# Backlog

Work items in approximate priority order. One item is taken at a time and
committed before the next begins.

Three conditions apply to every item. The unit tests and Android lint pass
before a commit is made, `requirements.md` is updated whenever an acceptance
criterion moves, and any decision not already recorded in `decision-log.md`
receives an entry there. The status of an item is open, in progress or done.

---

## B0, first build green. Done, 2026-09-14.

Synchronisation, unit tests and the debug build all pass. Completing this item
required an unplanned toolchain upgrade, described in the journal entry for
2026-09-14 and decided in ADR-0010, followed by bringing every pinned
dependency up to its current stable release, decided in ADR-0011. The debug
build was installed on a paired device and launched without incident,
satisfying the item's last remaining condition.

---

## B1, unit tests for the form validation rules. Done, 2026-09-14.

`GoalViewModelTest` covers a row with both fields empty being skipped, a row
with only one field filled being rejected, a minimum above the maximum being
rejected and the block length being clamped to the permitted range, alongside
a valid row being turned into a saved target. `LogEntryViewModelTest` covers a
blank exercise name, a value of zero for sets or repetitions and a missing
weight all being rejected, a comma being accepted as a decimal separator, and
selecting a muscle group as primary removing it from the secondary set.

Both test classes run against `FakeGoalRepository` and
`FakeSetEntryRepository` in `app/src/test/java/.../fake/`, in memory
implementations of the repository interfaces with no Room involved and no
Robolectric introduced. Each view model is constructed inside `@Before`,
after `Dispatchers.setMain` installs a test dispatcher, since `GoalViewModel`
already launches a coroutine on `viewModelScope` from its `init` block and
constructing it earlier, as a property initialiser, hits a missing main
dispatcher before the test dispatcher is installed.

The item is done when those cases are covered and acceptance criterion AC6
moves to done.

---

## B2, seed data for demonstrations and screenshots. Done, 2026-09-14.

`SeedDataGenerator` in `domain/logic` builds seven weeks of training history
across a fixed six day exercise split covering all twelve muscle groups, with
one week deliberately trained at three tenths of normal volume and the four
weeks after it timed to match the seeded goal's block length, so the deload
advisor reports the block as complete on the most recent week. A debug only
"Seed demo data" action on the dashboard, gated by `BuildConfig.DEBUG`, calls
it and writes the result through the existing repositories. Verified on a
freshly installed debug build on an emulator: a single tap takes the
dashboard from the empty state to 92 percent of the weekly goal with the
"Deload week is due" card shown, and the history screen lists all seven weeks
with the genuinely light week labelled a deload week at well under half the
surrounding volume.

---

## B3, Material 3 and accessibility pass. Open.

The application must visibly follow Material Design and the Android app quality
guidelines rather than merely use Material components. Every interactive
element requires a touch target of at least 48 density independent pixels and,
where it is not self explanatory, a content description. The light theme, the
dark theme and dynamic colour on Android 12 and above are each checked, as are
text scaling at 200 percent and landscape orientation. No user facing string
remains hardcoded, quantities use plural resources, and every screen has an
empty state, a loading state and an error state. The status colours in
`ui/theme/Color.kt` are verified in both themes, and TalkBack is run once over
the main flow.

The item is done when Android lint reports no accessibility warnings and
acceptance criterion AC7 moves to done.

---

## B4, logging reminder. Open.

An optional daily reminder, configured on the goal screen, is scheduled with
WorkManager and accompanied by a receiver for `BOOT_COMPLETED` so that the
schedule survives a restart. The notification opens `LogEntryActivity` through
an explicit intent. The `POST_NOTIFICATIONS` runtime permission is handled on
Android 13 and above, including the case in which the user declines it, and the
reminder is skipped on a day where something has already been logged. The
setting is stored with the goal rather than in a separate mechanism.

This item maps more directly onto units 5 and 6 of the course book than any
other in the backlog, which makes it worth more to the transfer criterion than
its size suggests. It introduces a dependency and therefore requires an ADR.

The item is done when the reminder fires, survives a reboot, opens the entry
screen, and behaves correctly when the permission is denied.

---

## B5, weekly volume trend. Open.

A chart shows accumulated weekly sets over the last eight to twelve weeks,
overall and for a selected muscle group, with the target range drawn as a band
behind the line so that the chart answers the same question as the dashboard.
It is drawn with a Compose `Canvas` in preference to a charting dependency; a
dependency would require an ADR. A textual summary of the trend accompanies the
chart for accessibility, and the colours are verified in both themes.

The item is done when the chart renders correctly with one week of data and
with twelve.

---

## B6, user interface test for the main flow. Open.

One instrumented Compose test covers the path a user actually takes: setting a
goal, logging an entry, and asserting that the dashboard shows the new volume
and the correct status for that muscle group. The test uses an in memory Room
database and the real view models, and asserts on behaviour rather than on
layout details such as padding or colour.

The item is done when `connectedDebugAndroidTest` passes on a device or
emulator.

---

## B7, Room migration and its test. Open, and urgent as soon as the schema changes.

The first schema change ships with a real migration rather than a destructive
one. The database version is raised, the `Migration` object is written, and the
exported schema is committed under `app/schemas`. A test using
`MigrationTestHelper` opens the previous schema, inserts a row, migrates, and
asserts that the row survived. Destructive migration is not enabled under any
circumstances.

---

## B8, source code documentation pass. Open.

The documentation must carry the reasoning rather than restate the code. Every
public type and every non obvious public function receives KDoc explaining why
it exists and which invariants it maintains. Constants that encode a domain
decision, such as the half set credit and the deload ratio, name the decision
and refer to the corresponding ADR. Comments that merely repeat the line below
them are removed.

The item is done when the domain layer can be read from beginning to end and
understood without the report open, satisfying acceptance criterion AC8.

---

## B9, edge cases. Open.

Several cases remain unhandled and each is worth a sentence in the evaluation
chapter. The week may roll over at midnight while the application is open, in
which case the dashboard should move to the new week. Entries may be dated in
the future or far in the past. Locale specific decimal separators appear in the
weight field. Implausible input such as 999 sets affects the progress
indicator. Deleting an entry is supported by the repository but exposed by no
screen.

Each case is either handled and covered by a test, or recorded in the journal
as a known limitation with a reason. A limitation that is named is worth more
in the report than a defect that is silent.

---

## B10, export the log as a comma separated values file. Optional.

The file is written to the cache directory and shared through a `FileProvider`
and an implicit intent, which provides a second clean example of intents for
the transfer chapter.

The item is done when the file opens correctly in a spreadsheet application and
the share sheet appears on a real device.

---

## B11, finishing the AGP 9 migration. Open, and blocking once AGP 10 is released.

Two compatibility flags remain in `gradle.properties`, each for a different
reason, and both are removed in AGP 10.

`android.newDsl=false` keeps the legacy variant application programming
interface available. Something in the build calls `applicationVariants`,
`testVariants` and `unitTestVariants`, and the caller is identified with
`./gradlew testDebugUnitTest -Pandroid.debug.obsoleteApi=true`. If the caller
is a plugin, a newer release of that plugin may already have dropped the legacy
interface. If it is the project's own build script, it is ported to
`androidComponents { onVariants { } }`. The flag is removed only afterwards.

`android.builtInKotlin=false` keeps the separate Kotlin Gradle plugin in place
instead of the Kotlin support built into AGP 9. Removing it entails dropping
`org.jetbrains.kotlin.android` from the build script, so it is treated as a
change of its own, after which KSP, the Compose compiler plugin and the unit
tests are each verified.

ADR-0011 raised Kotlin to 2.4.20 while AGP remained at 9.4.0, and that
pairing now also reports that `getByName("androidTest").assets.srcDirs(...)`
in `app/build.gradle.kts` is deprecated in favour of a `directories` mutable
set. It stems from the same legacy DSL compatibility mode as the two flags
above, so it is fixed in the same change, once the replacement API is
confirmed from AGP's own documentation rather than guessed at.

Once both flags are gone, the configuration cache is enabled through
`org.gradle.configuration-cache=true` and retained only if a clean build and
the tests both pass with it. Under no circumstances is any of this silenced
with `android.sync.suppressAgpWarnings`, since a suppressed warning becomes a
build failure at a less convenient moment.

The item is done when `./gradlew testDebugUnitTest` and `./gradlew
assembleDebug` run without deprecation warnings and no AGP compatibility flags
remain.
