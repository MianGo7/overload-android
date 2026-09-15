# Wireframes

Low fidelity wireframes of the five screens, drawn in PlantUML salt. Each
`.puml` file renders to the PNG that the report references from Appendix B:
`dashboard.png`, `goal.png`, `log-entry.png`, `history.png` and
`week-detail.png`. The navigation between the five screens is a separate
diagram, `docs/diagrams/activity-navigation.puml`.

These show the design as it was planned in `concept.md`, not the finished
application. They are kept in that state on purpose, because a wireframe
documents an intention and redrawing it to match the result destroys the
comparison. The differences are the material for the critical evaluation.

Differences between the planned design and the built application:

- The goal screen gained a daily reminder row, a time and an enable control,
  when the logging reminder was added, ADR-0014.
- The history screen gained a volume trend chart, ADR-0015, and an action to
  export the log as a CSV file, ADR-0016.
- The dashboard gained an action that seeds demo data, present in debug builds
  only and never in a release build.
- The status of a muscle group is shown as a coloured label with a progress
  indicator rather than the plain text column sketched here, and the colours
  were given separate dark theme values, ADR-0013.
