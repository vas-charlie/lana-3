#!/usr/bin/env python3
"""Verify the known-good LANA 3 launcher icon in source and built APKs."""

from __future__ import annotations

import argparse
import hashlib
import struct
import sys
import zipfile
from pathlib import Path


ICON = Path("androidApp/src/main/res/mipmap-xxxhdpi/ic_launcher.webp")
STALE_PNG = Path("androidApp/src/main/res/mipmap-xxxhdpi/ic_launcher.png")
MANIFEST = Path("androidApp/src/main/AndroidManifest.xml")
EXPECTED_SHA256 = "874d4db62be6f945b24cee7f1350debbc1d77a25a45897d1c08e4bcc1a112a24"


def fail(message: str) -> int:
    print(f"ERROR: {message}", file=sys.stderr)
    return 1


def sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def verify_webp_bytes(data: bytes, label: str) -> tuple[int, int]:
    if len(data) < 20:
        raise ValueError(f"{label}: WebP is too small/truncated")
    if data[0:4] != b"RIFF" or data[8:12] != b"WEBP":
        raise ValueError(f"{label}: invalid RIFF/WEBP signature")

    declared_size = struct.unpack("<I", data[4:8])[0] + 8
    if declared_size != len(data):
        raise ValueError(
            f"{label}: RIFF size mismatch: header expects {declared_size} bytes, "
            f"file contains {len(data)}"
        )

    offset = 12
    width = height = None
    saw_image_chunk = False

    while offset < len(data):
        if offset + 8 > len(data):
            raise ValueError(f"{label}: truncated WebP chunk header")

        chunk_type = data[offset:offset + 4]
        chunk_size = struct.unpack("<I", data[offset + 4:offset + 8])[0]
        payload_start = offset + 8
        payload_end = payload_start + chunk_size

        if payload_end > len(data):
            raise ValueError(
                f"{label}: truncated WebP chunk "
                f"{chunk_type.decode('ascii', 'replace')}"
            )

        payload = data[payload_start:payload_end]

        if chunk_type == b"VP8 ":
            if len(payload) < 10 or payload[3:6] != b"\x9d\x01\x2a":
                raise ValueError(f"{label}: invalid VP8 frame header")
            width = struct.unpack("<H", payload[6:8])[0] & 0x3FFF
            height = struct.unpack("<H", payload[8:10])[0] & 0x3FFF
            saw_image_chunk = True

        elif chunk_type == b"VP8L":
            if len(payload) < 5 or payload[0] != 0x2F:
                raise ValueError(f"{label}: invalid VP8L frame header")
            bits = int.from_bytes(payload[1:5], "little")
            width = (bits & 0x3FFF) + 1
            height = ((bits >> 14) & 0x3FFF) + 1
            saw_image_chunk = True

        elif chunk_type == b"VP8X":
            if len(payload) < 10:
                raise ValueError(f"{label}: invalid VP8X frame header")
            width = int.from_bytes(payload[4:7], "little") + 1
            height = int.from_bytes(payload[7:10], "little") + 1
            saw_image_chunk = True

        offset = payload_end + (chunk_size & 1)

    if offset != len(data):
        raise ValueError(f"{label}: invalid WebP padding/end offset")
    if not saw_image_chunk or width is None or height is None:
        raise ValueError(f"{label}: missing WebP image chunk")

    return width, height


def verify_known_good(data: bytes, label: str) -> tuple[int, int]:
    width, height = verify_webp_bytes(data, label)

    if (width, height) != (192, 192):
        raise ValueError(
            f"{label}: expected 192x192, got {width}x{height}"
        )

    digest = sha256(data)
    if digest != EXPECTED_SHA256:
        raise ValueError(
            f"{label}: SHA-256 mismatch; expected known-good "
            f"{EXPECTED_SHA256}, got {digest}"
        )

    return width, height


def verify_source() -> None:
    if STALE_PNG.exists():
        raise ValueError(
            "stale launcher PNG exists; clean rebuild must use one source asset"
        )
    if not ICON.exists():
        raise ValueError("official launcher WebP is missing")

    data = ICON.read_bytes()
    if len(data) < 3_000:
        raise ValueError("launcher WebP is suspiciously small")

    verify_known_good(data, str(ICON))

    manifest = MANIFEST.read_text(encoding="utf-8")
    for attr in (
        'android:icon="@mipmap/ic_launcher"',
        'android:roundIcon="@mipmap/ic_launcher"',
    ):
        if attr not in manifest:
            raise ValueError(f"manifest is missing {attr}")


def verify_apk(apk_path: Path) -> None:
    if not apk_path.exists():
        raise ValueError(f"APK does not exist: {apk_path}")

    with zipfile.ZipFile(apk_path) as apk:
        candidates = [
            name
            for name in apk.namelist()
            if name.startswith("res/") and name.endswith(".webp")
        ]
        exact_matches = []
        diagnostics = []

        for name in candidates:
            data = apk.read(name)
            digest = sha256(data)
            diagnostics.append(
                f"{name} size={len(data)} sha256={digest}"
            )
            if digest == EXPECTED_SHA256:
                exact_matches.append(name)

        if len(exact_matches) != 1:
            detail = "; ".join(diagnostics) if diagnostics else "none"
            raise ValueError(
                "expected exactly one byte-identical known-good packaged "
                f"launcher icon (release resource names may be obfuscated); "
                f"WebP candidates: {detail}"
            )

        name = exact_matches[0]
        verify_known_good(apk.read(name), f"{apk_path}:{name}")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--apk",
        type=Path,
        help="also verify the exact known-good launcher bytes inside this APK",
    )
    args = parser.parse_args()

    try:
        verify_source()
        if args.apk is not None:
            verify_apk(args.apk)
    except (OSError, ValueError, zipfile.BadZipFile) as error:
        return fail(str(error))

    suffix = f" and packaged APK {args.apk}" if args.apk else ""
    print(f"Verified known-good official launcher icon{suffix}.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
