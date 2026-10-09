# LANA 3 production avatar Godot prototype

Status: **prototype source only, NOT Android integrated, NOT an accepted avatar**.

The isolated Godot project lives under `godot/` so existing Android signing,
startup, and physical known-good baselines remain unchanged.

## Asset contract

Provide an original, licensed, rigged `godot/assets/lana_rigged.glb` with
facial morph targets named `eyeBlinkLeft`, `eyeBlinkRight`, `jawOpen`,
`mouthClose`, `mouthPucker`, `mouthStretchLeft`, `mouthRollLower`,
`mouthSmileLeft`, `mouthSmileRight`, `browInnerUp`.
The prototype deliberately reports unavailable without this asset. No image
is animated as a substitute. Missing channels are ignored, not faked.

The host's `AvatarRigPose` channels map into `apply_rig_pose(Dictionary)`.
A real Android-to-Godot plugin bridge and verified model import are **not yet
implemented**. Mouth poses require real playback-aligned viseme events;
Android TTS text-range callbacks are not visemes.

## Validation gates before Android integration

1. Import an actual rigged GLB and verify morph target names, materials,
   eye blink, lips, and head rotations in Godot on desktop.
2. Confirm original model identity, asset license, Android frame time,
   thermals and memory on S24 Ultra.
3. Export Godot Android AAR, integrate using the official Godot Android
   embedding sample and bridge host state to GDScript. Godot supports only
   one engine instance per Android process and requires careful
   configuration-change/lifecycle handling.
4. Test spoken Croatian audio against actual timed visemes, including
   mouth motion **only while Lana speaks**.
5. Build/sign with the same release signer as installed LANA 3, verify
   package/version/signer, test in-place update, then Charlie's physical
   acceptance.

Reference: https://docs.godotengine.org/en/stable/tutorials/platform/android/embedding_in_android_projects.html
Reference: https://docs.blender.org/manual/en/4.5/addons/import_export/scene_gltf2.html
