# LANA 3 — Godot avatar Android integration contract

## Goal
Embed the Godot 3D avatar as a presentation layer inside the existing Android application without replacing the app shell.

## Safety boundary
- Existing production installation and updater/signing path must remain untouched until a dedicated physical-device test build is approved.
- Android remains responsible for permissions, lifecycle, speech/input orchestration and app navigation.
- Godot renders the avatar and consumes semantic avatar state.

## Android → avatar state
The host must be able to send:
- listening / thinking / speaking / idle state
- speech activity
- viseme identifier and intensity
- facial-expression controls
- locale/language metadata when relevant

## Avatar → Android events
The embedded avatar may report:
- renderer ready
- model loaded / rejected
- missing required facial controls
- rendering/runtime error

## Model readiness gate
A model is not considered ready merely because a 3D mesh loads. It must satisfy the facial-control contract already enforced by the avatar validation layer.

## Integration acceptance criteria
1. Godot view can be hosted without replacing the existing Android app.
2. App lifecycle pause/resume does not leave the renderer in an invalid state.
3. Avatar state can be driven without UI buttons.
4. Missing/invalid avatar assets fail safely and report a semantic error.
5. Existing updater/signing behavior is unchanged.
6. No S24 production install is touched by this integration work.

## Next implementation slice
Create the Android host adapter and Godot bridge against this contract, then cover the bridge with CI tests before producing a physical-device build.
