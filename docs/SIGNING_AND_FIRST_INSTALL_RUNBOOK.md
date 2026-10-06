# LANA 3 — Signing and first signed install runbook

## Purpose

This runbook is for the one-time private signing setup and the first signed Android installation.

The signing key is a long-term identity for LANA 3 updates. Do not generate a replacement key merely because a local command is unavailable. Recover and use the existing key and its matching credentials whenever possible.

## Current repository contract

The GitHub workflow expects exactly these repository secrets:

- `LANA_SIGNING_KEY_B64`
- `LANA_SIGNING_STORE_PASSWORD`
- `LANA_SIGNING_KEY_ALIAS`
- `LANA_SIGNING_KEY_PASSWORD`

The workflow restores the private keystore only inside the GitHub runner, builds a release APK, verifies its signature with `apksigner`, and publishes `lana-3.apk` as the latest GitHub Release asset.

The private keystore itself must never be committed to Git.

## Before touching the key

1. Confirm that the existing private signing key backup is available.
2. Confirm that the matching store password, key alias and key password are available from the secure secret record.
3. Do not paste secret values into chat screenshots, GitHub issues, commits, README files or normal notes.
4. If any of those values are uncertain, stop. Do not create a new key until the existing signing identity has been deliberately ruled out.

## Windows keytool troubleshooting

A `keytool is not recognized` message means Windows cannot find Java's `keytool.exe` through PATH. It does **not** mean the keystore is broken.

First try:

```bat
where keytool
```

If nothing is returned, check whether Java/Android Studio provides it. Common locations include:

```text
C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe
C:\Program Files\Java\<jdk-folder>\bin\keytool.exe
```

Use an exact existing path rather than changing system PATH during signing setup unless there is a separate reason to do so.

If the signing secret values are already known and the GitHub keystore secret already exists, local `keytool` is not required merely to enter the remaining GitHub secrets.

## GitHub secret setup

Repository path:

`lana-3 → Settings/Postavke → Secrets and variables/Tajne i varijable → Actions/Radnje`

Add or verify each expected secret by **name**. GitHub will not show an existing secret value after it is saved.

Never delete or overwrite the existing Base64 keystore secret unless there is a verified reason.

## First workflow run

After all four secrets exist:

1. Open `Actions/Radnje`.
2. Open `LANA 3 CI`.
3. Run the workflow on `main`, or use the next normal push to `main`.
4. Verify that `shared-core`, `security-baseline` and `android-preview` all pass.
5. Verify that `android-signed-release` does not merely report success with its build steps skipped.
6. Confirm these signing steps actually run:
   - Restore private signing key
   - Build signed update APK
   - Verify APK signature
   - Publish latest LANA update

## Release verification

A successful signed release must create a GitHub Release with:

- tag form `dev-<number>`;
- asset named exactly `lana-3.apk`;
- a successful APK signature verification step.

Do not install until those checks pass.

## First device installation

Before first signed installation:

- If no LANA 3 build is installed, install the signed APK normally.
- If an old debug build with the same package name is installed, Android may reject the signed release because the signatures differ. Do not uninstall anything containing important local data without checking first.
- Record the installed version before testing future automatic updates.

Future updates must be signed with the **same signing identity**. Losing the private key would break normal update continuity.

## DONE gate

Signing is not DONE until:

- all four GitHub secrets are configured;
- signed release steps actually execute;
- signature verification passes;
- `lana-3.apk` is published;
- first signed installation succeeds on a physical Android device;
- a later signed release successfully updates over the first one.
