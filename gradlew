#!/usr/bin/env sh
set -eu

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION=9.2.1
GRADLE_DIR="$APP_HOME/.gradle/gradle-$GRADLE_VERSION"
GRADLE_BIN="$GRADLE_DIR/bin/gradle"

if [ ! -x "$GRADLE_BIN" ]; then
  if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
  fi
  ZIP="$APP_HOME/.gradle/gradle-$GRADLE_VERSION-bin.zip"
  mkdir -p "$APP_HOME/.gradle"
  if [ ! -f "$ZIP" ]; then
    if command -v curl >/dev/null 2>&1; then
      curl -L -o "$ZIP" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
    elif command -v wget >/dev/null 2>&1; then
      wget -O "$ZIP" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
    else
      echo "Instale Gradle ou curl/wget para baixar o Gradle $GRADLE_VERSION." >&2
      exit 1
    fi
  fi
  unzip -q "$ZIP" -d "$APP_HOME/.gradle"
fi

exec "$GRADLE_BIN" "$@"
