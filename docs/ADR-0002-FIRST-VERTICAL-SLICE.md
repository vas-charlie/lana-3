# ADR-0002 — First vertical slice

- **Status:** ACCEPTED FOR IMPLEMENTATION
- **Date:** 2026-10-04

## Decision
The first executable proof of LANA 3 begins in the shared core, before Maps/Uber/Bolt or device automation.

The first slice proves:
1. language-neutral intent model;
2. typed context;
3. Decision Engine;
4. authorization output;
5. deterministic taxi calculation;
6. "Lana does not guess" behavior;
7. Unicode/non-Croatian input in shared models;
8. unit tests independent of Samsung/Android APIs.

## Why
This is the smallest useful slice that tests the architectural foundation without hiding failures behind external services or UI.

## Acceptance
- shared core compiles;
- shared tests pass;
- unknown intent returns CannotDecide;
- taxi calculation uses total movement, not only passenger kilometers;
- Unicode text passes through the model;
- no Android/Samsung dependency exists in shared core.

## Important limitation
A committed test is not a passed test. CI/local Gradle execution must verify this before the slice can move to TEST/GOTOVO.
