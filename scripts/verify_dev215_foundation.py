#!/usr/bin/env python3
"""Protect the Android foundation physically accepted on dev-215.

This is intentionally semantic rather than a full-file lock: unrelated UI/features may
change, while the packaging/startup contract that previously caused a non-launchable APK
must remain present.
"""

from __future__ import annotations

import re
import sys
from pathlib import Path


ROOT_BUILD = Path("build.gradle.kts")
ANDROID_BUILD = Path("androidApp/build.gradle.kts")
MANIFEST = Path("androidApp/src/main/AndroidManifest.xml")
MAIN_ACTIVITY = Path(
    "androidApp/src/main/java/hr/vascharlie/lana3/MainActivity.kt"
)


def fail(message: str) -> int:
    print(f"ERROR: dev-215 foundation regression: {message}", file=sys.stderr)
    return 1


def require(text: str, needle: str, where: str) -> None:
    if needle not in text:
        raise ValueError(f"{where} is missing required contract: {needle}")


def main() -> int:
    try:
        root = ROOT_BUILD.read_text(encoding="utf-8")
        android = ANDROID_BUILD.read_text(encoding="utf-8")
        manifest = MANIFEST.read_text(encoding="utf-8")
        activity = MAIN_ACTIVITY.read_text(encoding="utf-8")

        # Root/build plugin contract. This omission caused the pre-dev-205 APKs
        # to reference MainActivity in the manifest without packaging its Kotlin class.
        require(root, 'kotlin("android") version "2.4.20" apply false', str(ROOT_BUILD))
        require(android, 'kotlin("android")', str(ANDROID_BUILD))
        require(android, 'sourceCompatibility = JavaVersion.VERSION_17', str(ANDROID_BUILD))
        require(android, 'targetCompatibility = JavaVersion.VERSION_17', str(ANDROID_BUILD))
        require(android, 'buildConfig = true', str(ANDROID_BUILD))

        # Identity/platform contract.
        require(android, 'applicationId = "hr.vascharlie.lana3"', str(ANDROID_BUILD))
        require(android, 'minSdk = 26', str(ANDROID_BUILD))
        require(android, 'targetSdk = 36', str(ANDROID_BUILD))
        require(android, 'compileSdk = 36', str(ANDROID_BUILD))

        # Launcher/startup contract.
        require(manifest, 'android:name=".MainActivity"', str(MANIFEST))
        require(manifest, 'android:exported="true"', str(MANIFEST))
        require(manifest, '<action android:name="android.intent.action.MAIN" />', str(MANIFEST))
        require(manifest, '<category android:name="android.intent.category.LAUNCHER" />', str(MANIFEST))
        require(manifest, 'android:icon="@mipmap/ic_launcher"', str(MANIFEST))
        require(manifest, 'android:roundIcon="@mipmap/ic_launcher"', str(MANIFEST))

        # The physically exercised six-state developer-preview contract.
        state_match = re.search(
            r"enum class LanaVisualState\s*\{([^}]*)\}",
            activity,
            re.DOTALL,
        )
        if not state_match:
            raise ValueError("MainActivity no longer declares LanaVisualState")

        states = {
            token.strip()
            for token in state_match.group(1).split(",")
            if token.strip()
        }
        required_states = {
            "IDLE",
            "LISTENING",
            "THINKING",
            "SPEAKING",
            "OFFLINE",
            "ERROR",
        }
        missing_states = sorted(required_states - states)
        if missing_states:
            raise ValueError(
                "MainActivity lost physically exercised visual states: "
                + ", ".join(missing_states)
            )

        for state in sorted(required_states):
            require(
                activity,
                f"LanaVisualState.{state} ->",
                str(MAIN_ACTIVITY),
            )

        # Keep startup failures visible instead of silently crashing during development.
        require(activity, "showStartupFailure(startupStage, error)", str(MAIN_ACTIVITY))

    except (OSError, ValueError) as error:
        return fail(str(error))

    print("Verified dev-215 Android foundation contract.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
