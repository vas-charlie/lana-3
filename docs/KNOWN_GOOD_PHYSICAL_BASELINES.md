# LANA 3 — KNOWN-GOOD / PHYSICAL ACCEPTANCE LOG

## 2026-10-08 — Developer Preview 215

### Identity

- Release: `dev-215`
- Version shown on device: `0.1.215-dev`
- Commit: `0d7b577c4bed46663df8ab8a3ed4fd2b957b39be`
- APK SHA-256: `bcb7dfc31cb5c6b5a07e6b6bef668859b3640b914e7ae52c7a455c9de091b406`
- Physical device: Samsung SM-S928B
- Android: 16

### Physically confirmed on Charlie's device

- [x] Signed APK installs.
- [x] App launches without the previous immediate startup crash.
- [x] App stays open during the tested interaction.
- [x] Developer Preview version shown in-app matches build 215.
- [x] Manual visual-state cycle works through:
  - IDLE
  - LISTENING
  - THINKING
  - SPEAKING
  - OFFLINE
  - ERROR
  - return to IDLE
- [x] Each manual state changes the visible state label/status instead of crashing.

### Explicitly NOT yet accepted

- [ ] Background → foreground lifecycle.
- [ ] Normal close/reopen repetition.
- [ ] Rotation/configuration changes.
- [ ] Launcher icon appearance on the physical launcher.
- [ ] Camera permission/use.
- [ ] Microphone permission/use.
- [ ] Location permission/use.
- [ ] Real voice recognition.
- [ ] Real TTS response.
- [ ] Smart Ride physical acceptance.
- [ ] Notes Lab physical acceptance.
- [ ] Offline behavior.
- [ ] Signed in-app updater.
- [ ] Real Lana avatar/presence. Build 215 still uses a text placeholder in the avatar stage.

## Stability rule

The confirmed startup/package baseline above is now **known-good for the properties explicitly checked**.

Do not casually change the Android packaging, Kotlin Android plugin setup, startup activity path, signing setup, or the verified APK-class regression gate while working on unrelated features.

A future change may replace or extend these components only with a specific reason and must preserve the already confirmed behavior.

This record does **not** declare the whole app stable. It freezes only the evidence-backed parts.
