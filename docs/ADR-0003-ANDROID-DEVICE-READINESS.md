# ADR-0003 — Android device readiness and permission setup

- **Status:** CI PASSED — AWAITING PHYSICAL-DEVICE TEST
- **Date:** 2026-10-06
- **Verified CI run:** 37453080660
- **Target branch:** `feature/android-device-readiness`

## Decision

The Android developer preview gets a small platform-readiness layer before camera, voice, location, screen awareness, or background automation are connected.

The first Android readiness slice must:

1. detect the current Android device without hard-coding Samsung models;
2. distinguish phone/tablet form factor by Android configuration;
3. report camera, microphone, and location capability honestly;
4. request only the runtime permissions needed for capabilities that exist on the device;
5. never request camera, microphone, or location automatically just because the application launched;
6. keep a visible user-triggered readiness control for requesting missing sensor permissions;
7. let feature-specific surfaces request sensitive permissions contextually when the user invokes that feature;
8. treat approximate location as degraded rather than pretending precise location exists;
9. avoid requesting screen-capture permission until the user actually invokes a screen-awareness feature.

## Why

LANA 3 is device-independent by architecture, but real Android permissions and hardware capabilities must be proven on physical devices. The first physical target is Charlie's current phone, with the future tablet used as another test target rather than a product boundary.

This slice creates a reusable platform probe so later camera, voice, and location adapters can consume measured capability state instead of guessing from device brand or model.

## Security and privacy

- Camera, microphone, and location remain under Android runtime permission control.
- No permission is silently escalated.
- App launch itself does not trigger camera, microphone, or location permission prompts.
- The user can explicitly request missing sensor permissions from the readiness control.
- Feature-specific surfaces should request only the permission needed for the action the user just invoked. Voice Lab already follows this pattern for microphone access.
- A denied permission remains denied until the user explicitly retries or changes Android settings; LANA does not nag on every launch.
- Screen capture is intentionally excluded because Android requires a separate user-approved MediaProjection flow when screen access is actually needed.
- Contacts, phone, messages, and other sensitive permissions are not requested before the corresponding feature exists.

## Acceptance criteria

- [x] Android developer preview compiles in CI.
- [x] Existing shared-core tests continue to pass.
- [ ] First launch does not automatically request camera/microphone/location permissions.
- [ ] Manual readiness button explicitly requests only missing permissions for capabilities available on the device.
- [ ] Voice Lab requests microphone access only after the user invokes listening.
- [ ] Relaunch does not automatically repeat a denied permission request.
- [ ] UI shows the actual device model, Android version, form factor, RAM, and sensor permission state.
- [ ] Approximate location is shown as approximate, not precise.
- [ ] Device without a capability reports it as unavailable instead of requesting a useless permission.
- [ ] Physical-device behavior is verified on at least one phone before acceptance.

## CI evidence

GitHub Actions run **37453080660** completed both relevant jobs successfully:
- `shared-core` — success
- `android-preview` — success, including APK build and artifact upload

## Lifecycle status

Specification → Architecture → Contextual-permission refinement → **IN TEST** → Physical-device test → Charlie acceptance
