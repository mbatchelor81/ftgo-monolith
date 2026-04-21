# Architecture Decision Records

This directory captures FTGO architecture decisions using the
[MADR](https://adr.github.io/madr/) convention. Each record is immutable once
accepted — supersede it with a new ADR instead of editing in place.

## Index

| ID   | Title                                      | Status   |
|------|--------------------------------------------|----------|
| 0001 | Repository structure for microservices     | Accepted |

## Authoring a new ADR

1. Copy [`0000-template.md`](0000-template.md) to
   `<NNNN>-<short-slug>.md` using the next free number.
2. Fill in the Context, Decision, Consequences, and Alternatives sections.
3. Open a PR. Reviewers should focus on whether the *decision* is sound — not
   the prose.
4. When merged, mark the status **Accepted** and update the index above.

## Conventions

- ID is zero-padded to 4 digits (`0001`, `0002`, …).
- File name is `<NNNN>-<kebab-case-slug>.md`.
- Status transitions: `Proposed → Accepted`, then optionally
  `Accepted → Superseded` (by an ADR with a higher number) or
  `Accepted → Deprecated` (no replacement).
