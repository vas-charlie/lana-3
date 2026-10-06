# ADR-0012 — Language-neutral note validation reasons

- **Status:** IN TEST
- **Date:** 2026-10-06
- **Target branch:** `i18n/note-validation-reasons`

## Decision

Shared note validation no longer returns human-language error sentences.

`NoteResult.Invalid` carries a `NoteInvalidReason` enum. Android presentation maps that semantic reason to localized Croatian/English resources.

Current reasons cover:

- blank note text;
- blank note identifier;
- blank search query;
- unauthorized note execution;
- a non-note action reaching the note executor.

## Why

The shared core is multilingual by architecture. A domain service should not bake English UI copy into validation results.

Semantic reasons also make tests more reliable because they assert a stable contract rather than searching message text.

## Boundary

This slice covers note validation only.

Other shared-core English reason/explanation strings remain technical debt and must migrate incrementally in separate tested changes rather than one large rewrite.

## Acceptance

- [x] note-service tests assert semantic validation reasons;
- [ ] shared-core CI passes;
- [ ] Android Notes Lab compiles with localized reason mapping;
- [ ] Android lint passes;
- [ ] physical-device locale behavior remains pending.
