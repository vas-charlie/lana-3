# LANA 3: production avatar technology gate (2026-10-09)

Status: RESEARCH / NOT SELECTED. Do not equate the current Canvas scaffold with the intended realistic Lana avatar.

## Confirmed facts

- Android TTS `onRangeStart` is optional: it fires only if the active TTS engine supplies timing. It reports text ranges, not phonemes or visemes. Source: https://developer.android.com/reference/android/speech/tts/UtteranceProgressListener
- Android TTS also exposes `onBeginSynthesis` and `onAudioAvailable` (PCM chunks), but these may arrive before playback; chunks alone cannot be treated as playback-synchronized mouth shapes. Source: https://developer.android.com/reference/android/speech/tts/UtteranceProgressListener
- Godot supports embedding a 3D engine inside an existing Android application using an Android library/AAR, including glTF model display. The stable documentation warns about one engine instance per process and resizing/orientation caveats. Source: https://docs.godotengine.org/en/stable/tutorials/platform/android/android_library.html
- Unity Personal is listed free under a USD 200,000 revenue/funding threshold, subject to eligibility. However Unity's own pricing page describes Personal as intended for gaming/entertainment, so LANA's commercial AI assistant use case requires license validation before adoption. Source: https://unity.com/products

## Candidate evaluation, not a decision

1. Rigged 3D avatar with independent morph targets/blend shapes and skeletal channels, built in Blender; evaluate Godot embedded Android renderer first because an integration path is documented. Need verify realistic facial fidelity, blendshape control, packaging size, memory, battery, thermal performance, and license obligations on real devices.
2. Unity rigged-avatar renderer: potentially strong tooling, but do not choose until non-game commercial licensing and embedding into existing Kotlin app are resolved.
3. Current Android Canvas vector view: development-only semantic-state scaffold; NOT production renderer or final visual identity.

## Audio / lip-sync requirement

Production-grade mouth movement must be driven by playback-aligned phoneme/viseme timestamps, or a validated audio-driven facial solver, rather than word-length guesses. A voice engine's support for Croatian, latency, cost, data privacy, offline behavior, and reliable timing metadata must be tested. Keep a renderer-neutral stream of timed viseme events so TTS and avatar engines remain replaceable.

## Explicit acceptance gate

Do not mark DONE or spend money until: a realistic original Lana model exists with independent eye/eyelid/face/jaw/head channels; selected engine can render in the existing Android app; Croatian speech timing drives corresponding mouth shapes; Charlie speech never moves Lana's mouth; app builds and runs on phone and tablet; measured performance and costs are acceptable; Charlie physically tests and accepts.

## Immediate next step

Prototype the **renderer integration and model/viseme contract**, without locking in a purchased avatar tool or claiming realistic lip-sync. Before selecting a renderer, verify Godot's blendshape/glTF import behavior and actual Android integration against the current build pipeline. Existing PR #73 is only a speech-range cue, not true lip-sync.
