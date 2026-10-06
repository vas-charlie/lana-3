# ADR-0004 — Voice ports and Android voice lab

- **Status:** IN TEST
- **Date:** 2026-10-06
- **Target branch:** `feature/voice-test-foundation`

## Decision

LANA 3 voice input and output are defined through shared, platform-neutral ports.

The first Android adapter uses the operating system's installed speech-recognition and text-to-speech services only as a test implementation. Android is not allowed to leak into the shared speech contract.

## Shared contracts

The shared module defines:

- `SpeechInputPort`
- `SpeechOutputPort`
- language-neutral request models
- partial/final transcript events
- structured error events
- explicit availability

A requested language is represented by an optional BCP-47 language tag such as `hr-HR`, `en-US`, or `de-DE`. No fixed list of supported languages is embedded in the shared core.

## Android test adapter

The developer preview gets a Voice Lab screen that can:

1. start Android speech recognition after microphone permission is granted;
2. show partial and final transcripts;
3. stop or cancel listening;
4. send entered/transcribed text to Android TTS;
5. change the requested language tag manually;
6. report unsupported/unavailable services instead of pretending voice works.

## Important limits

- This is **not** yet proof of production-grade continuous voice.
- The Android recognizer may use local or network processing depending on the installed recognition service and device configuration.
- Language availability and quality are not assumed.
- Automatic language detection is not implemented by this slice.
- Bluetooth routing, wake word, background listening, interruption handling, echo cancellation, and driving-condition tests remain separate work.
- TTS initialization is asynchronous; the test UI may briefly report that TTS is still preparing.
- Physical-device testing is required before acceptance.

## Privacy and safety

- Microphone access remains behind Android runtime permission.
- Denied microphone permission blocks listening.
- No audio recording is stored by LANA in this slice.
- The shared ports carry transcripts/events, not raw Android objects.
- Voice input does not grant permission to execute unrelated actions.

## Acceptance criteria

- [ ] shared speech contracts compile in KMP shared core;
- [ ] Android adapter compiles without Android types leaking into shared code;
- [ ] Voice Lab is reachable from the developer preview;
- [ ] microphone denial produces an explicit blocked state;
- [ ] speech recognizer unavailability is reported honestly;
- [ ] partial/final transcript flow compiles;
- [ ] TTS unsupported language produces a structured error;
- [ ] Android developer preview CI passes;
- [ ] physical STT/TTS test passes on at least one real Android device;
- [ ] at least one non-Croatian language is physically tested before voice is accepted.

## Lifecycle status

Specification → Architecture → Implementation → **IN TEST** → Physical-device test → Charlie acceptance
