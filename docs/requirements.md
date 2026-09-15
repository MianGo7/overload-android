# Requirements and acceptance criteria

Source: task description for DLBCSEMSE02, task 1, individual tracking app.
A criterion is only ticked when it is implemented and covered by a test, a
screenshot or a demonstrable screen. Keep this table honest, it is the
traceability the quality criterion of the grade asks for.

## Acceptance criteria from the task

| Id | Criterion | Status | Where |
| --- | --- | --- | --- |
| AC1 | The app lets the user specify their individual goal | done | `ui/goal`, phase, accumulation block length and a set range per muscle group; `domain/model/TrainingGoal.kt` validates its own input; covered by `GoalViewModelTest` |
| AC2 | The app offers screens to enter performance data | done | `ui/logentry`, eight fields per entry; validation covered by `LogEntryViewModelTest` |
| AC3 | The app stores the data | done | `data/local`, Room database `overload.db` at version 2, exported schemas, `MIGRATION_1_2` covered by `AppDatabaseMigrationTest`, queries covered by `SetEntryDaoTest` |
| AC4 | The app evaluates progress and reports the current state | done | `domain/logic/VolumeCalculator.kt`, `ProgressEvaluator.kt` and `DeloadAdvisor.kt`, each unit tested; rendered by `ui/dashboard` and `ui/history` |
| AC5 | The app implements several Android activities | done | five activities in `AndroidManifest.xml`, navigation by explicit intent, week passed as `EXTRA_WEEK_START_EPOCH_DAY` |
| AC6 | The app is tested using unit tests | done | `app/src/test` for domain logic and the two form view models against fake repositories; `app/src/androidTest` for Room queries and migrations, plus one instrumented Compose test, `MainFlowTest`, covering the goal, log entry and dashboard screens together against a real in memory database |
| AC7 | The app is easy to use and follows Material Design and the Android app quality guidelines | done | Material 3 theme with checked light and dark contrast, ADR-0013; content descriptions, empty and loading states, plural resources, verified touch targets, 200 percent text scale and landscape |
| AC8 | The source code documentation is appropriate | done | KDoc on every public domain type and non obvious function, explaining why rather than restating the code; the constants that encode a domain decision, the half set credit, the deload ratio, the Monday week start and the accumulation block length, each reference the ADR or carry the reasoning inline |
| AC9 | All code, resources and configuration needed to build, test and deploy are in a GitHub repository, link in the report | done | github.com/MianGo7/overload-android, with a GitHub Actions workflow running the unit tests, Android lint and a debug build |

## Report deliverables from the task

| Id | Deliverable | Status |
| --- | --- | --- |
| RD1 | Brief concept written before implementation | drafted in `docs/concept.md`, to be rewritten for the report |
| RD2 | Wireframes of the initial app design | drawn, five files in `docs/wireframes`, to be rendered for Appendix B |
| RD3 | Overview of the software design and how components interact | three diagrams in `docs/diagrams`, chapter 3 drafted |
| RD4 | Explained extracts of the core source code | open |
| RD5 | Critical evaluation of whether the app fulfils the target functionality | open |
| RD6 | Possible future improvements | open, material in `docs/decision-log.md` |
| RD7 | Lessons learned | open, material in `docs/dev-journal.md` |

Open criteria are turned into scoped work items in `docs/backlog.md`. The
full paraphrase of what the examination asks for is in
`docs/course-context.local.md`, which is not committed.

## Self imposed quality bar

- Domain logic has no Android dependency and is unit tested.
- No hardcoded user facing strings.
- Every architectural decision has an entry in the decision log.
- The build runs from a clean checkout with `./gradlew testDebugUnitTest`.
