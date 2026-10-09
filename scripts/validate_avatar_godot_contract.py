#!/usr/bin/env python3
from pathlib import Path
import re
import sys

script = Path("godot/avatar/lana_avatar.gd")
scene = Path("godot/avatar/lana_avatar.tscn")
project = Path("godot/project.godot")
for path in (script, scene, project):
    if not path.is_file():
        raise SystemExit(f"missing Godot avatar file: {path}")

text = script.read_text(encoding="utf-8")
required_channels = {
    "EYE_BLINK_LEFT", "EYE_BLINK_RIGHT", "BROW_RAISE_LEFT", "BROW_RAISE_RIGHT",
    "JAW_OPEN", "LIPS_CLOSED", "LIPS_WIDE", "LIPS_ROUNDED",
    "LIPS_LOWER_BITE", "SMILE_LEFT", "SMILE_RIGHT",
    "HEAD_YAW", "HEAD_PITCH", "HEAD_ROLL",
}
missing = sorted(c for c in required_channels if f'"{c}"' not in text)
if missing:
    raise SystemExit("missing rig channels: " + ", ".join(missing))

if 'res://assets/lana_rigged.glb' not in text:
    raise SystemExit("production GLB contract path changed unexpectedly")
if "func is_production_model_ready()" not in text:
    raise SystemExit("renderer must expose truthful model readiness")
if "push_warning" not in text:
    raise SystemExit("missing-asset state must be explicit")

scene_text = scene.read_text(encoding="utf-8")
for node in ("ModelRoot", "Camera3D", "KeyLight"):
    if f'name="{node}"' not in scene_text:
        raise SystemExit(f"missing scene node: {node}")

print("Godot avatar source contract: OK")
print("Note: this validates source structure only; it does not validate a GLB, Godot runtime, Android embedding, performance, signing, or physical behavior.")
