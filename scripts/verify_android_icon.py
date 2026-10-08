#!/usr/bin/env python3
"""Verify the official Android launcher icon source before building the APK."""

from __future__ import annotations

import struct
import sys
from pathlib import Path


ICON = Path("androidApp/src/main/res/mipmap-xxxhdpi/ic_launcher.webp")
STALE_PNG = Path("androidApp/src/main/res/mipmap-xxxhdpi/ic_launcher.png")
MANIFEST = Path("androidApp/src/main/AndroidManifest.xml")


def fail(message: str) -> int:
    print(f"ERROR: {message}", file=sys.stderr)
    return 1


def verify_webp(path: Path) -> tuple[int, int]:
    data = path.read_bytes()

    if len(data) < 20:
        raise ValueError("WebP is too small/truncated")
    if data[0:4] != b"RIFF" or data[8:12] != b"WEBP":
        raise ValueError("invalid RIFF/WEBP signature")

    declared_size = struct.unpack("<I", data[4:8])[0] + 8
    if declared_size != len(data):
        raise ValueError(
            f"RIFF size mismatch: header expects {declared_size} bytes, "
            f"file contains {len(data)}"
        )

    offset = 12
    width = height = None
    saw_image_chunk = False

    while offset < len(data):
        if offset + 8 > len(data):
            raise ValueError("truncated WebP chunk header")

        chunk_type = data[offset:offset + 4]
        chunk_size = struct.unpack("<I", data[offset + 4:offset + 8])[0]
        payload_start = offset + 8
        payload_end = payload_start + chunk_size

        if payload_end > len(data):
            raise ValueError(
                f"truncated WebP chunk {chunk_type.decode('ascii', 'replace')}"
            )

        payload = data[payload_start:payload_end]

        if chunk_type == b"VP8 ":
            if len(payload) < 10 or payload[3:6] != b"\x9d\x01\x2a":
                raise ValueError("invalid VP8 frame header")
            width = struct.unpack("<H", payload[6:8])[0] & 0x3FFF
            height = struct.unpack("<H", payload[8:10])[0] & 0x3FFF
            saw_image_chunk = True

        elif chunk_type == b"VP8L":
            if len(payload) < 5 or payload[0] != 0x2F:
                raise ValueError("invalid VP8L frame header")
            bits = int.from_bytes(payload[1:5], "little")
            width = (bits & 0x3FFF) + 1
            height = ((bits >> 14) & 0x3FFF) + 1
            saw_image_chunk = True

        elif chunk_type == b"VP8X":
            if len(payload) < 10:
                raise ValueError("invalid VP8X frame header")
            width = int.from_bytes(payload[4:7], "little") + 1
            height = int.from_bytes(payload[7:10], "little") + 1
            saw_image_chunk = True

        offset = payload_end + (chunk_size & 1)

    if offset != len(data):
        raise ValueError("invalid WebP padding/end offset")
    if not saw_image_chunk or width is None or height is None:
        raise ValueError("missing WebP image chunk")

    return width, height


def main() -> int:
    if STALE_PNG.exists():
        return fail("stale launcher PNG exists; clean rebuild must use one source asset")
    if not ICON.exists():
        return fail("official launcher WebP is missing")

    try:
        width, height = verify_webp(ICON)
    except (OSError, ValueError) as error:
        return fail(f"launcher WebP failed integrity check: {error}")

    if (width, height) != (192, 192):
        return fail(
            f"launcher WebP must be 192x192 for xxxhdpi, got {width}x{height}"
        )

    if ICON.stat().st_size < 3_000:
        return fail("launcher WebP is suspiciously small")

    manifest = MANIFEST.read_text(encoding="utf-8")
    for attr in (
        'android:icon="@mipmap/ic_launcher"',
        'android:roundIcon="@mipmap/ic_launcher"',
    ):
        if attr not in manifest:
            return fail(f"manifest is missing {attr}")

    print(
        f"Verified official launcher icon: {ICON} "
        f"({width}x{height}, {ICON.stat().st_size} bytes)"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
