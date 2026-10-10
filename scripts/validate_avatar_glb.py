#!/usr/bin/env python3
"""Validate a candidate LANA rigged GLB before Godot/Android integration."""
import json, struct, sys
from pathlib import Path

REQUIRED_MORPHS = {"eyeBlinkLeft","eyeBlinkRight","jawOpen","mouthClose","mouthPucker","mouthStretchLeft","browInnerUp","mouthRollLower","mouthSmileLeft","mouthSmileRight"}

def fail(message):
    raise SystemExit("avatar GLB rejected: " + message)

def read_glb_json(path):
    data = path.read_bytes()
    if len(data) < 20: fail("file is too small to be a GLB")
    magic, version, declared_length = struct.unpack_from("<4sII", data, 0)
    if magic != b"glTF": fail("invalid GLB magic")
    if version != 2: fail(f"unsupported glTF version {version}; expected 2")
    if declared_length != len(data): fail("declared GLB length does not match file size")
    chunk_length, chunk_type = struct.unpack_from("<II", data, 12)
    if chunk_type != 0x4E4F534A: fail("first GLB chunk is not JSON")
    end = 20 + chunk_length
    if end > len(data): fail("JSON chunk exceeds file size")
    try:
        return json.loads(data[20:end].decode("utf-8").rstrip(" \t\r\n\x00"))
    except (UnicodeDecodeError, json.JSONDecodeError) as exc:
        fail(f"invalid JSON chunk: {exc}")

def collect_real_morph_names(document):
    names = set()
    for mesh in document.get("meshes", []):
        target_names = [str(name) for name in (mesh.get("extras") or {}).get("targetNames", [])]
        primitives = mesh.get("primitives") or []
        target_count = max((len(primitive.get("targets") or []) for primitive in primitives), default=0)
        if target_names and target_count == 0:
            fail("mesh advertises morph names but contains no morph targets")
        if target_count and len(target_names) != target_count:
            fail(f"mesh morph-name count ({len(target_names)}) does not match target count ({target_count})")
        names.update(target_names)
    return names

def main():
    if len(sys.argv) != 2:
        raise SystemExit("usage: validate_avatar_glb.py <candidate.glb>")
    path = Path(sys.argv[1])
    if not path.is_file(): fail(f"missing candidate file: {path}")
    document = read_glb_json(path)
    names = collect_real_morph_names(document)
    missing = sorted(REQUIRED_MORPHS - names)
    if missing: fail("missing required independent facial morphs: " + ", ".join(missing))
    print("Avatar GLB structural gate: OK")
    print("Required facial morphs:", ", ".join(sorted(REQUIRED_MORPHS)))
    print("Note: this does not prove visual identity, licensing, materials, runtime performance, or physical-device acceptance.")

if __name__ == "__main__":
    main()
