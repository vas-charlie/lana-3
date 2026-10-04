# ADR-0002 — First vertical slice

- **Status:** TEST PASSED — AWAITING CHARLIE ACCEPTANCE
- **Date:** 2026-10-04
- **Verified CI run:** 37190484791
- **Verified commit:** ed9ba73dfa565621880bc59bbd8d3a838ce008a0

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

## Acceptance evidence

- [x] shared core compiles;
- [x] shared tests pass;
- [x] unknown intent returns CannotDecide;
- [x] taxi calculation uses total movement, not only passenger kilometers;
- [x] Unicode text passes through the model;
- [x] no Android/Samsung dependency is required by the tested shared core;
- [x] GitHub CI independently compiles and executes `:shared:desktopTest`.

## Verification

GitHub Actions run **37190484791** completed successfully after the CI actions were updated.

Verified job: **shared-core**

Verified steps:
- Checkout — success
- Java 17 setup — success
- Gradle setup — success
- Compile and test shared core — success

The preceding successful run also recorded `BUILD SUCCESSFUL` for `:shared:desktopTest`.

## Lifecycle status

Specification → Architecture → Implementation → Integration → **TEST PASSED** → Acceptance

This slice is **not marked GOTOVO automatically**. Under the LANA 3 lifecycle, Charlie's acceptance remains a separate gate.

## Next engineering gate

After acceptance, begin the next vertical slice without weakening this regression test. Every subsequent change to `main` continues to run the shared-core CI automatically.
