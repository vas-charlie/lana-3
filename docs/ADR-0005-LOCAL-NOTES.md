# ADR-0005 — Local notes foundation

- **Status:** IN TEST
- **Date:** 2026-10-06
- **Target branch:** `feature/local-notes-foundation`

## Decision

LANA 3 gets an offline-first notes foundation with a shared, platform-neutral service and an Android SQLite adapter.

The shared layer owns note validation and note use cases. Android owns the persistence mechanism.

## Shared note model

The shared core defines:

- `LanaNote`
- `NoteRepository`
- `NoteService`
- explicit result types for saved, updated, deleted, found, not found, and invalid operations

The first slice supports:

1. create;
2. update;
3. delete;
4. list;
5. simple text search;
6. Unicode note content;
7. deterministic timestamps and IDs supplied from outside the core.

Blank notes and blank search queries are rejected rather than guessed.

## Android persistence

The first Android adapter uses a private SQLite database named `lana_notes.db`.

Schema version 1 stores:

- note ID;
- note text;
- creation timestamp;
- last-update timestamp.

The database is private to the application. This slice does not sync notes to a cloud service.

A destructive migration is deliberately not implemented. If the schema version changes later, an explicit migration must be designed rather than silently deleting user notes.

## Android Notes Lab

The developer preview gets a test screen for:

- creating notes;
- editing notes;
- deleting notes;
- listing local notes;
- searching text.

This is a development surface, not the final LANA notes UI.

## Important limits

- Note links to clients, rides, reservations, or dates are not yet modeled.
- Voice-to-note composition is not yet integrated with the Voice Lab.
- Full-text indexing and language-specific search are not yet implemented.
- Sync across devices is not part of this slice.
- Encryption beyond Android application-private storage is not claimed yet.
- Production database threading/performance policy remains future work.

## Acceptance criteria

- [ ] shared note service compiles and passes unit tests;
- [ ] Unicode note text survives shared save/update flow unchanged;
- [ ] blank notes are rejected;
- [ ] missing note update/delete returns NotFound;
- [ ] Android SQLite adapter compiles behind `NoteRepository`;
- [ ] Notes Lab is reachable from the Android developer preview;
- [ ] Android developer preview CI passes;
- [ ] create/update/delete/list/search are physically verified on one Android device;
- [ ] app restart physically proves local persistence before acceptance.

## Lifecycle status

Specification → Architecture → Implementation → **IN TEST** → Physical-device persistence test → Charlie acceptance
