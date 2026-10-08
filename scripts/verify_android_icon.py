#!/usr/bin/env python3
"""Verify the official Android launcher icon source before building the APK."""

from __future__ import annotations

import binascii
import struct
import sys
from pathlib import Path


ICON = Path("androidApp/src/main/res/mipmap-xxxhdpi/ic_launcher.png")
OLD_ICON = Path("androidApp/src/main/res/mipmap-xxxhdpi/ic_launcher.webp")
MANIFEST = Path("androidApp/src/main/AndroidManifest.xml")
PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"


def fail(message: str) -> int:
    print(f"ERROR: {message}", file=sys.stderr)
    return 1


def verify_png(path: Path) -> tuple[int, int]:
    data = path.read_bytes()
    if len(data) < 33:
        raise ValueError("PNG is too small/truncated")
    if not data.startswith(PNG_SIGNATURE):
        raise ValueError("invalid PNG signature")

    offset = len(PNG_SIGNATURE)
    width = height = None
    saw_iend = False

    while offset < len(data):
        if offset + 12 > len(data):
            raise ValueError("truncated PNG chunk header")

        length = struct.unpack(">I", data[offset:offset + 4])[0]
        chunk_type = data[offset + 4:offset + 8]
        chunk_start = offset + 8
        chunk_end = chunk_start + length
        crc_end = chunk_end + 4

        if crc_end > len(data):
            raise ValueError(
                f"truncated PNG chunk {chunk_type.decode('ascii', 'replace')}"
            )

        chunk_data = data[chunk_start:chunk_end]
        expected_crc = struct.unpack(">I", data[chunk_end:crc_end])[0]
        actual_crc = binascii.crc32(chunk_type)
        actual_crc = binascii.crc32(chunk_data, actual_crc) & 0xFFFFFFFF

        if actual_crc != expected_crc:
            raise ValueError(
                f"CRC mismatch in {chunk_type.decode('ascii', 'replace')}"
            )

        if chunk_type == b"IHDR":
            if length != 13:
                raise ValueError("invalid IHDR length")
            width, height = struct.unpack(">II", chunk_data[:8])

        if chunk_type == b"IEND":
            saw_iend = True
            if crc_end != len(data):
                raise ValueError("unexpected bytes after IEND")
            break

        offset = crc_end

    if not saw_iend:
        raise ValueError("missing IEND chunk")
    if width is None or height is None:
        raise ValueError("missing IHDR chunk")

    return width, height


def main() -> int:
    if OLD_ICON.exists():
        return fail(
            "obsolete launcher WebP still exists; clean rebuild requires it removed"
        )
    if not ICON.exists():
        return fail("official launcher PNG is missing")

    try:
        width, height = verify_png(ICON)
    except (OSError, ValueError) as error:
        return fail(f"launcher PNG failed integrity check: {error}")

    if (width, height) != (192, 192):
        return fail(
            f"launcher PNG must be 192x192 for xxxhdpi, got {width}x{height}"
        )

    if ICON.stat().st_size < 10_000:
        return fail("launcher PNG is suspiciously small")

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
