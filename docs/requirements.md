# Requirements and acceptance criteria

Source: task description for DLBCSEMSE02, task 1, individual tracking app.
A criterion is only ticked when it is implemented and covered by a test, a
screenshot or a demonstrable screen. Keep this table honest, it is the
traceability the quality criterion of the grade asks for.

## Acceptance criteria from the task

| Id | Criterion | Status | Where |
| --- | --- | --- | --- |
| AC1 | The app lets the user specify their individual goal | scaffolded | `ui/goal`, `domain/model/TrainingGoal.kt` |
| AC2 | The app offers screens to enter performance data | scaffolded | `ui/logentry` |
| AC3 | The app stores the data | scaffolded | `data/local`, Room database `overload.db` |
| AC4 | The app evaluates progress and reports the current state | scaffolded | `domain/logic/ProgressEvaluator.kt`, `ui/dashboard` |
| AC5 | The app implements several Android activities | scaffolded | 5 activities, see `AndroidManifest.xml` |
| AC6 | The app is tested using unit tests | done | `app/src/test`, domain logic and the two form view models covered against fake repositories |
| AC7 | The app is easy to use and follows Material Design and the Android app quality guidelines | open | Material 3 theme in place, needs a review pass |
| AC8 | The source code documentation is appropriate | partly | KDoc on public domain and data types |
| AC9 | All code, resources and configuration needed to build, test and deploy are in a GitHub repository, link in the report | open | repository is local only so far |

## Report deliverables from the task

| Id | Deliverable | Status |
| --- | --- | --- |
| RD1 | Brief concept written before implementation | draft in `docs/concept.md` |
| RD2 | Wireframes of the initial app design | open, see `docs/wireframes` |
| RD3 | Overview of the software design and how components interact | open, source material in `docs/decision-log.md` |
| RD4 | Explained extracts of the core source code | open |
| RD5 | Critical evaluation of whether the app fulfils the target functionality | open |
| RD6 | Possible future improvements | open |
| RD7 | Lessons learned | open, source material in `docs/dev-journal.md` |

Open criteria are turned into scoped work items in `docs/backlog.md`. The
full paraphrase of what the examination asks for is in
`docs/course-context.local.md`, which is not committed.

## Self imposed quality bar

- Domain logic has no Android dependency and is unit tested.
- No hardcoded user facing strings.
- Every architectural decision has an entry in the decision log.
- The build runs from a clean checkout with `./gradlew testDebugUnitTest`.
