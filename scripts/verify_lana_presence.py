#!/usr/bin/env python3
from pathlib import Path
import struct
import sys

ASSET = Path("androidApp/src/main/res/drawable-nodpi/lana_presence.webp")
V2_ASSET = Path("androidApp/src/main/assets/lana_presence_v2.webp.b64")
MAIN = Path("androidApp/src/main/java/hr/vascharlie/lana3/MainActivity.kt")

def fail(message: str) -> None:
    print(f"::error::{message}")
    raise SystemExit(1)

if not ASSET.is_file():
    fail(f"Missing Lana presence asset: {ASSET}")

data = ASSET.read_bytes()
if len(data) < 1024:
    fail("Lana presence asset is unexpectedly small.")
if data[:4] != b"RIFF" or data[8:12] != b"WEBP":
    fail("Lana presence asset is not a RIFF/WEBP file.")

declared = struct.unpack_from("<I", data, 4)[0] + 8
if declared != len(data):
    fail(f"WEBP RIFF size mismatch: declared={declared}, actual={len(data)}")

width = height = None
offset = 12
while offset + 8 <= len(data):
    chunk = data[offset:offset+4]
    size = struct.unpack_from("<I", data, offset + 4)[0]
    payload = offset + 8
    end = payload + size
    if end > len(data):
        fail("WEBP chunk extends beyond file length.")

    if chunk == b"VP8X" and size >= 10:
        width = 1 + int.from_bytes(data[payload+4:payload+7], "little")
        height = 1 + int.from_bytes(data[payload+7:payload+10], "little")
        break
    if chunk == b"VP8 " and size >= 10:
        marker = data.find(b"\x9d\x01\x2a", payload, end)
        if marker != -1 and marker + 7 <= end:
            width = struct.unpack_from("<H", data, marker + 3)[0] & 0x3FFF
            height = struct.unpack_from("<H", data, marker + 5)[0] & 0x3FFF
            break
    if chunk == b"VP8L" and size >= 5 and data[payload] == 0x2F:
        bits = int.from_bytes(data[payload+1:payload+5], "little")
        width = (bits & 0x3FFF) + 1
        height = ((bits >> 14) & 0x3FFF) + 1
        break

    offset = end + (size & 1)

if width is None or height is None:
    fail("Could not determine Lana presence WEBP dimensions.")
if width < 200 or height < 250:
    fail(f"Lana presence asset is too small: {width}x{height}")

import base64

if not V2_ASSET.is_file():
    fail(f"Missing Lana v2 presence asset: {V2_ASSET}")

try:
    v2_data = base64.b64decode(V2_ASSET.read_text(encoding="ascii"), validate=True)
except Exception as exc:
    fail(f"Lana v2 presence asset is not valid base64: {exc}")

if len(v2_data) < 4096:
    fail("Lana v2 presence asset is unexpectedly small.")
if v2_data[:4] != b"RIFF" or v2_data[8:12] != b"WEBP":
    fail("Lana v2 decoded presence asset is not a RIFF/WEBP file.")

main = MAIN.read_text(encoding="utf-8")
required = [
    "private lateinit var avatar: ImageView",
    "lana_presence_v2.webp.b64",
    "R.drawable.lana_presence",
    "ImageView.ScaleType.CENTER_CROP",
    "avatar_content_description",
]
for token in required:
    if token not in main:
        fail(f"MainActivity no longer renders the Lana presence asset: missing {token!r}")

if "text = getString(R.string.avatar_name)" in main:
    fail("Legacy text-only LANA placeholder returned to the avatar stage.")

print(
    f"Lana presence verified: fallback={width}x{height}/{len(data)} bytes, "
    f"v2={len(v2_data)} decoded bytes"
)
