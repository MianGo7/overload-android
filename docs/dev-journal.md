# Development journal

Chronological record of the work. One entry per working session, newest last.
This is the main source for the process criterion of the grade and for the
lessons learned chapter, so write entries while the detail is still fresh,
including the things that did not work.

Template for a new entry:

    ## YYYY-MM-DD, short title
    Worked on:
    Decisions:
    Problems and how they were solved:
    Next:

---

## 2026-09-13, project setup

**Worked on.** Created the repository skeleton for the tracking app: Gradle
build with a version catalogue and a committed wrapper, the Android
application module, the domain layer with its unit tests, the Room data layer,
the manual dependency container, five activities with their Compose screens
and view models, and the documentation set in `docs/`.

The starting specification was fixed before any code was written: task 1 of
the examination task, training volume as the application domain, Compose with
several activities, Room with manual dependency injection.

**Decisions.** ADR-0001 to ADR-0009 were recorded, covering the platform
choice, the multi activity structure, Room, manual dependency injection,
pinned versions, fractional counting of indirect volume, the Monday week
boundary, the backup policy and where aggregation happens.

**Problems and how they were solved.** The project was laid out before the
first Gradle sync, so nothing had been compiled at the time of writing. The
mitigation was to pin conservative dependency versions that are known to work
together and to keep the Compose code to stable APIs. The first sync is
therefore the first real build.

**Next.**

1. Open the project in Android Studio, let it sync, run
   `./gradlew testDebugUnitTest` and fix whatever the first build reports.
2. Draw the wireframes for the five screens and place them in
   `docs/wireframes`.
3. Run the app on a device, log a week of real training and note where the
   entry flow is slow.
4. Add unit tests for the view model validation rules.
5. Create the GitHub repository and push, then record the link in the report.

---

## 2026-09-13, planning documents

**Worked on.** Added the documents that make the project workable session by
session: `docs/next-steps.md` with the immediate steps, `docs/backlog.md` with
eleven scoped work items from getting the first build green to an optional CSV
export, and a local summary of the examination requirements, the grading
criteria and the formal rules for the report.

**Decisions.** The summary of the examination requirements is excluded from
version control through a `*.local.md` rule. IU holds the copyright on its
examination tasks and objects to them being published on third party
platforms, and this repository will be linked in the report, so the assignment
material stays off the remote while remaining available locally. Local editor
and tooling configuration is kept untracked for the same reason of keeping the
repository to the project itself.

**Next.** Items B0 to B2 in `docs/backlog.md`, and the personal steps in
`docs/next-steps.md`, starting with the first Gradle sync.
