#!/usr/bin/env python3
"""Verify that required Android application classes are actually packaged in an APK."""

from __future__ import annotations

import argparse
import struct
import sys
import zipfile


REQUIRED_CLASSES = {
    "Lhr/vascharlie/lana3/MainActivity;",
    "Lhr/vascharlie/lana3/SmartRideTestActivity;",
    "Lhr/vascharlie/lana3/VoiceTestActivity;",
    "Lhr/vascharlie/lana3/NotesTestActivity;",
}


def u32(data: bytes, offset: int) -> int:
    return struct.unpack_from("<I", data, offset)[0]


def read_uleb128(data: bytes, offset: int) -> tuple[int, int]:
    result = 0
    shift = 0
    while True:
        value = data[offset]
        offset += 1
        result |= (value & 0x7F) << shift
        if value < 0x80:
            return result, offset
        shift += 7
        if shift > 35:
            raise ValueError("Invalid ULEB128 value")


def dex_classes(data: bytes) -> set[str]:
    if not data.startswith(b"dex\n"):
        raise ValueError("Not a DEX file")

    string_ids_size = u32(data, 56)
    string_ids_off = u32(data, 60)
    type_ids_size = u32(data, 64)
    type_ids_off = u32(data, 68)
    class_defs_size = u32(data, 96)
    class_defs_off = u32(data, 100)

    strings: list[str] = []
    for index in range(string_ids_size):
        string_data_off = u32(data, string_ids_off + index * 4)
        _, cursor = read_uleb128(data, string_data_off)
        end = data.index(0, cursor)
        strings.append(data[cursor:end].decode("utf-8", errors="replace"))

    types = [
        strings[u32(data, type_ids_off + index * 4)]
        for index in range(type_ids_size)
    ]

    return {
        types[u32(data, class_defs_off + index * 32)]
        for index in range(class_defs_size)
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("apk")
    args = parser.parse_args()

    packaged_classes: set[str] = set()
    with zipfile.ZipFile(args.apk) as apk:
        dex_names = sorted(
            name for name in apk.namelist()
            if name.startswith("classes") and name.endswith(".dex")
        )
        if not dex_names:
            print("ERROR: APK contains no DEX files.", file=sys.stderr)
            return 1

        for dex_name in dex_names:
            packaged_classes.update(dex_classes(apk.read(dex_name)))

    missing = sorted(REQUIRED_CLASSES - packaged_classes)
    if missing:
        print("ERROR: Android application classes are missing from the APK:", file=sys.stderr)
        for class_name in missing:
            print(f"  - {class_name}", file=sys.stderr)
        return 1

    print("Verified required Android application classes in APK:")
    for class_name in sorted(REQUIRED_CLASSES):
        print(f"  - {class_name}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
