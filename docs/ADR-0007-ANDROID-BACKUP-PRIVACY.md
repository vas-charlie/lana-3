# ADR-0007 — Android backup privacy baseline

- **Status:** IN TEST
- **Date:** 2026-10-06
- **Target branch:** `security/disable-implicit-android-backup`

## Decision

LANA 3 must not rely on Android's implicit cloud backup or device-to-device transfer for local application data.

The Android application keeps `android:allowBackup="false"` and also supplies explicit backup/data-extraction exclusion rules for platform versions where device-to-device behavior can differ from the legacy `allowBackup` switch.

## Why

LANA is expected to hold increasingly sensitive local state such as:

- private notes;
- application preferences;
- capability/permission state;
- future task and context state;
- update metadata.

Implicit OS backup is not the same thing as a designed LANA sync system. A future multi-device sync feature must have its own data model, authorization, encryption, conflict handling, and user controls.

## Android rules

For Android 12 and newer, `data_extraction_rules.xml` excludes application root, files, databases, shared preferences, external app data, and device-protected equivalents from both cloud backup and device transfer.

For Android 11 and older, `backup_rules.xml` excludes the corresponding legacy backup domains.

## Important limits

- This does not encrypt the local SQLite database by itself.
- This does not implement LANA account sync.
- This does not prevent a rooted/compromised device from reading app data.
- Physical restore/transfer behavior remains a device test item.

## Acceptance criteria

- [ ] Android lint accepts the manifest and rules.
- [ ] Android developer preview builds in CI.
- [ ] Existing shared-core tests remain green.
- [ ] App-private notes remain available during normal app restart.
- [ ] Future cross-device sync does not silently reuse Android backup as its transport.

## Lifecycle status

Architecture → Implementation → **IN TEST** → Physical-device privacy test → Charlie acceptance
