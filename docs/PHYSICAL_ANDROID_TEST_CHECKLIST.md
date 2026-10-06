# LANA 3 — Physical Android test checklist

## Rule

A CI-green feature is not automatically physically accepted. Record what happened on the real device. Do not convert a failure into a guessed success.

## 1. First launch and lifecycle

- [ ] Signed APK installs successfully.
- [ ] App launches without crash.
- [ ] Version shown in Developer Preview matches the installed build.
- [ ] App survives background → foreground.
- [ ] App survives normal close/reopen.
- [ ] Rotation or configuration changes do not create an obvious broken state.
- [ ] IDLE avatar micro-motion is subtle and stops/changes correctly with other visual states.

## 2. Permissions and capability truthfulness

Test camera, microphone and location separately.

- [ ] First app launch does not automatically prompt for camera, microphone, or location.
- [ ] Readiness control requests missing sensor permissions only after the user taps it.
- [ ] Voice Lab requests microphone only after the user starts listening.
- [ ] Permission prompt appears when the corresponding action is explicitly invoked.
- [ ] Denying a permission does not produce false “ready” state.
- [ ] Granting permission updates readiness.
- [ ] Approximate location is reported as approximate.
- [ ] Precise location is reported as precise.
- [ ] Missing/unavailable hardware is handled without crash.

## 3. Voice Lab

Croatian:

- [ ] `hr-HR` recognition starts.
- [ ] Partial/final transcript behavior is sensible.
- [ ] TTS speaks Croatian.
- [ ] Stop/cancel works.

Second language:

- [ ] Test at least one non-Croatian language, initially `en-US`.
- [ ] Unsupported/unavailable behavior is reported honestly.

Safety:

- [ ] Recognized speech is not silently saved as a note.
- [ ] Voice-to-note opens a reviewable prefill.

## 4. Notes Lab

- [ ] Create note.
- [ ] Restart app and verify note remains.
- [ ] Search.
- [ ] Edit.
- [ ] Delete.
- [ ] Blank note is rejected.
- [ ] Voice-prefilled text still requires explicit save.
- [ ] No note text appears in normal diagnostic logging.

## 5. Smart Ride

Use at least two known manual examples.

- [ ] Empty thresholds are rejected rather than invented.
- [ ] Invalid/impossible inputs are rejected.
- [ ] €/km result matches manual calculation.
- [ ] €/h result matches manual calculation.
- [ ] Pickup distance/time is included.
- [ ] ACCEPT / CONSIDER / SKIP follows the entered thresholds.
- [ ] No Uber/Bolt action is executed automatically.

## 6. Offline/degraded behavior

- [ ] Put device in airplane mode.
- [ ] Local notes still work.
- [ ] Smart Ride deterministic calculation still works.
- [ ] Update check reports unavailable rather than hanging or pretending success.
- [ ] Voice behavior matches the actual installed Android speech service capability.

## 7. Signed updater

After the first signed build is installed, publish a later signed build.

- [ ] LANA detects a newer release.
- [ ] APK downloads.
- [ ] Package name check passes.
- [ ] Version progression check passes.
- [ ] Signing identity check passes.
- [ ] Android asks the user for final installation approval.
- [ ] Update installs over the existing signed build.
- [ ] Existing local notes survive the update.
- [ ] App reports up-to-date afterward.

## 8. Privacy and backup boundary

- [ ] Local data behaves normally across app restart.
- [ ] No secret values appear in screenshots/log output used for testing.
- [ ] Device-transfer/cloud-backup behavior is tested separately before being declared accepted.
- [ ] Future multi-device sync is not confused with Android implicit backup.

## Acceptance record

For each failed item, capture:

- build/version;
- device and Android version;
- exact step;
- observed result;
- screenshot/log only if it contains no secrets;
- whether failure is reproducible.

Only then move the relevant feature from **IN TEST** toward Charlie acceptance.
