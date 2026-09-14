# Development journal

A chronological record of the work, one entry per session, written on the same
day. Each entry states what was built, what was decided, which problems
occurred and how they were resolved. These entries are the main source for the
process criterion and for the lessons learned chapter, so difficulties are
recorded as they occurred rather than summarised comfortably afterwards.

---

## 2026-09-13

The repository skeleton was created. It comprises a Gradle build with a
version catalogue and a committed wrapper, the Android application module, the
domain layer together with its unit tests, the Room data layer, the manual
dependency container, five activities with their Compose screens and view
models, and the documentation set under `docs/`. The specification was fixed
before any code was written, namely task 1 of the examination task, training
volume as the application domain, Jetpack Compose with several activities, and
Room with a hand written dependency container rather than a framework.

No Gradle synchronisation had been performed at this point, so nothing had yet
been compiled. Dependency versions were therefore pinned conservatively to
combinations known to work together, and the user interface was restricted to
stable Compose interfaces. The intention was that the first synchronisation
would surface at most a small number of failures rather than several unrelated
ones at once.

The planning documents followed later the same day: the personal checklist in
`next-steps.md`, the backlog, and a local summary of what the examination
requires. That summary is excluded from version control through the
`*.local.md` rule, because IU holds the copyright on its examination tasks and
objects to their publication on third party platforms, and the repository will
be linked in the report.

The next step was the first synchronisation.

---

## 2026-09-14

Opening the project in Android Studio for the first time triggered the Android
Gradle plugin (AGP) upgrade assistant, which raised Gradle from 8.11.1 to
9.6.0, AGP from 8.7.3 to 9.4.0, Kotlin from 2.0.21 to 2.2.10 and Kotlin Symbol
Processing (KSP) from 2.0.21-1.0.28 to 2.3.6. Room was left at 2.6.1. The
first build consequently failed in the `kspDebugKotlin` task with
`unexpected jvm signature V`, which is a known error produced when KSP2
processes an annotation processor written before KSP2 existed. Room gained
KSP2 support in version 2.7.0, so the mismatch was the entire cause. Raising
Room to 2.8.5 resolved it and the unit tests then passed. The reasoning behind
moving the toolchain forward instead of pinning it back is recorded as
ADR-0010.

The second problem of the session was the volume of deprecation warnings the
upgrade assistant left behind. It had added a set of `android.*` compatibility
flags that preserve AGP 8 behaviour, five of which this project does not need:
the manifest declares no `uses-sdk` element, `targetSdk` is set explicitly, no
`resValue` is used anywhere, and resource shrinking is disabled. Those five
were removed rather than suppressed, and
`android.dependency.excludeLibraryComponentsFromConstraints` was enabled,
which is what the repeated performance warning had asked for. Two remaining
flags, `android.builtInKotlin` and `android.newDsl`, were left in place
because each represents a migration of its own, and both are tracked as
backlog item B11.

Two Kotlin warnings concerned the `@StringRes` annotations on the constructor
properties of `GoalViewModel` and `LogEntryViewModel`, where the annotation
target is changing in a future Kotlin release. The recommended option was
taken, opting in to the new behaviour through
`-Xannotation-default-target=param-property`, which in turn required replacing
the deprecated `kotlinOptions` block with the `kotlin { compilerOptions { } }`
extension.

Android Studio additionally removed the redundant `android:label` attribute
from the launcher activity, since it duplicates the label already declared on
the application element, and moved the adaptive icons from
`mipmap-anydpi-v26` to `mipmap-anydpi`, since a minimum software development
kit (SDK) level of 26 makes the version qualifier meaningless. Both changes
were kept.

A further pass through `lintDebug` afterwards reported twelve more warnings
concerning version currency rather than compatibility: Kotlin, KSP, the
AndroidX core, lifecycle, activity and test libraries, the Jetpack Compose
bill of materials (BOM), coroutines-test, the Gradle wrapper, and a
`compileSdk` value below the ceiling AGP 9.4 supports. Every one was raised to
its current stable release, recorded as ADR-0011. Kotlin was taken to 2.4.20
despite an open issue in the KSP tracker suggesting that version was not yet
fully supported, a concern resolved by running the build rather than trusting
the tracker: `kspDebugKotlin` and Room's annotation processing both completed
without error. The `-Xannotation-default-target=param-property` flag from
earlier in the session was removed in the same change, since Kotlin 2.4's
default language version already applies that behaviour. Three
`PluralsCandidate` warnings remain on strings that format a count inline, a
resource design question rather than a defect, and the version bump exposed a
further deprecation warning about `srcDirs` on the `androidTest` asset source
set, tracked alongside backlog item B11 since it stems from the same legacy
DSL compatibility mode as the two flags already recorded there. A Pixel
device was paired over wireless debugging afterwards, and `installDebug`
put the app on it without incident, closing out B0's remaining criterion.

B1 was taken next instead. `GoalViewModelTest` and `LogEntryViewModelTest`
cover the validation rules listed in the backlog item, run against two new in
memory fakes, `FakeGoalRepository` and `FakeSetEntryRepository`, under
`app/src/test/java/.../fake/`. The one problem worth recording: two
`GoalViewModelTest` cases initially failed with the saved goal staying null,
because the view model was built as a property initialiser, which runs before
JUnit's `@Before` method. `GoalViewModel` already launches a coroutine on
`viewModelScope` from its `init` block, so it hit a missing main dispatcher
before `Dispatchers.setMain` had a chance to install the test one. Moving the
view model's construction into `@Before`, after the dispatcher is set,
resolved it. Acceptance criterion AC6 moved to done.

The continuous integration (CI) pipeline on GitHub Actions was failing on
every run with `Unable to download toolchain matching the requirements
({languageVersion=21, vendor=JetBrains, ...})`. The cause was
`gradle/gradle-daemon-jvm.properties`, committed since the first commit and
generated by Android Studio's `updateDaemonJvm` task, which pins the Gradle
daemon itself, not just Kotlin compilation, to a JetBrains Runtime version 21.
That toolchain is not distributed through the Foojay catalogue the project's
toolchain resolver plugin downloads from, so it resolved locally only because
Android Studio's own bundled JetBrains Runtime happened to already be
installed on the development machine, and failed on a runner that only has
Temurin 17. The file was deleted, reverting the daemon to Gradle's ordinary
default of running on whatever JDK is on the path, which is exactly what the
workflow's `setup-java` step already provides. A lesson for the evaluation
chapter: a file an IDE assistant generates silently is not necessarily
portable, and a green local build does not prove a green pipeline.

The next item is B11, which finishes the AGP 9 migration.
