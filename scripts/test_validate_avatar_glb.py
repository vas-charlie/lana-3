#!/usr/bin/env python3
"""Self-test for the avatar GLB structural gate."""
import json, struct, subprocess, sys, tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
VALIDATOR = ROOT / "scripts" / "validate_avatar_glb.py"
REQUIRED = ["eyeBlinkLeft","eyeBlinkRight","jawOpen","mouthClose","mouthPucker","mouthStretchLeft","browInnerUp"]

def write_glb(path, names, target_count):
    doc={"asset":{"version":"2.0"},"meshes":[{"extras":{"targetNames":names},"primitives":[{"attributes":{"POSITION":0},"targets":[{} for _ in range(target_count)]}]}]}
    raw=json.dumps(doc,separators=(",",":")).encode()
    raw += b" " * ((4-len(raw)%4)%4)
    chunk=struct.pack("<II",len(raw),0x4E4F534A)+raw
    path.write_bytes(struct.pack("<4sII",b"glTF",2,12+len(chunk))+chunk)

def run(path):
    return subprocess.run([sys.executable,str(VALIDATOR),str(path)],capture_output=True,text=True)

with tempfile.TemporaryDirectory() as d:
    d=Path(d)
    good=d/"good.glb"; write_glb(good,REQUIRED,len(REQUIRED))
    result=run(good)
    assert result.returncode==0, result.stderr+result.stdout

    fake=d/"names-only.glb"; write_glb(fake,REQUIRED,0)
    result=run(fake)
    assert result.returncode!=0, "names-only GLB must be rejected"

    missing=d/"missing-wink.glb"; names=[n for n in REQUIRED if n!="eyeBlinkRight"]; write_glb(missing,names,len(names))
    result=run(missing)
    assert result.returncode!=0 and "eyeBlinkRight" in (result.stderr+result.stdout), "missing independent right-eye blink must be rejected"

print("Avatar GLB validator self-test: OK")
