# ADR-0009 — Deterministic Android update policy

- **Status:** IN TEST
- **Date:** 2026-10-06
- **Target branch:** `core/update-policy`

## Decision

Version-selection behavior for LANA 3 updates is deterministic shared-core policy rather than Android UI logic.

The policy receives:

- currently installed version code;
- latest remote version code;
- pending download version, if any;
- pending download id, if any.

It returns one of:

- `UpToDate`
- `Download`
- `ResumeExistingDownload`

## Rules

- A remote version equal to the installed version is up to date.
- A remote version older than the installed version never triggers a downgrade.
- A newer version downloads when there is no valid matching pending download.
- A matching pending version with a valid download id is resumed instead of duplicated.
- A stale pending version does not block a newer release.

## Separation of responsibility

`UpdatePolicy` decides version behavior only.

Android remains responsible for:

- GitHub release retrieval;
- DownloadManager;
- package/version/signature verification;
- unknown-source permission;
- invoking the Android installer.

The policy does not grant install permission and cannot bypass Android user confirmation.

## Acceptance

- [x] shared-core regression tests cover same-version, downgrade, newer, resume, stale and invalid-pending cases;
- [ ] Android CI builds with the policy integrated;
- [ ] physical signed updater test remains pending.
