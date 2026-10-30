#!/usr/bin/env bash
#
# Builds dist/SafakSayar.jar and dist/SafakSayar.jad for MIDP 2.0 / CLDC 1.1.
#
# The phone needs Java 1.4 bytecode (class file version 48), so this script
# uses a JDK 8 compiler with -source/-target 1.4 and ProGuard's CLDC
# preverification. If JDK 8 is not installed it is downloaded once into
# .tools/jdk8 (Temurin, ~100 MB).
#
# Usage:
#   ./build.sh          build only
#   ./build.sh test     run the date tests, then build
#
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

APP_NAME="SafakSayar"
MAIN_CLASS="com.safaksayar.SafakSayarMIDlet"
DIST="$ROOT/dist"
BUILD="$ROOT/build"
LIB="$ROOT/lib"
TOOLS="$ROOT/.tools"

mkdir -p "$DIST" "$BUILD" "$TOOLS"

# ---------------------------------------------------------------- JDK 8 ---
find_jdk8() {
    if [ -n "${JAVA8_HOME:-}" ] && [ -x "$JAVA8_HOME/bin/javac" ]; then
        echo "$JAVA8_HOME"
        return
    fi
    if [ -x "$TOOLS/jdk8/bin/javac" ]; then
        echo "$TOOLS/jdk8"
        return
    fi
    if command -v javac >/dev/null 2>&1; then
        local v
        v="$(javac -version 2>&1 | awk '{print $2}')"
        case "$v" in
            1.8.*|8.*)
                dirname "$(dirname "$(readlink -f "$(command -v javac)")")"
                return
                ;;
        esac
    fi
}

download_jdk8() {
    local arch
    arch="$(uname -m)"
    case "$arch" in
        x86_64|amd64) arch="x64" ;;
        aarch64|arm64) arch="aarch64" ;;
        *) echo "Unsupported CPU: $arch" >&2; exit 1 ;;
    esac

    echo "JDK 8 not found; downloading Temurin 8 ($arch) into .tools/jdk8 ..."
    rm -rf "$TOOLS/jdk8" "$TOOLS/jdk8.tar.gz"
    mkdir -p "$TOOLS/jdk8"
    curl -fL --retry 3 \
        -o "$TOOLS/jdk8.tar.gz" \
        "https://api.adoptium.net/v3/binary/latest/8/ga/linux/$arch/jdk/hotspot/normal/eclipse"
    tar xzf "$TOOLS/jdk8.tar.gz" -C "$TOOLS/jdk8" --strip-components=1
    rm -f "$TOOLS/jdk8.tar.gz"
    echo "$TOOLS/jdk8"
}

JDK="$(find_jdk8 || true)"
if [ -z "$JDK" ]; then
    JDK="$(download_jdk8)"
fi
if [ ! -x "$JDK/bin/javac" ]; then
    echo "No usable JDK 8 found. Set JAVA8_HOME to a JDK 8 installation." >&2
    exit 1
fi
echo "Using JDK: $JDK"

# --------------------------------------------------------------- tools ---
if [ ! -f "$LIB/cldc-1.1-stub.jar" ] || [ ! -f "$LIB/midp-2.0-stub.jar" ]; then
    echo "Downloading MIDP/CLDC API stubs ..."
    mkdir -p "$LIB"
    curl -fL -o "$LIB/cldc-1.1-stub.jar" \
        "https://repo1.maven.org/maven2/com/github/mcpat/apistubs/cldc-1.1-stub/1.0/cldc-1.1-stub-1.0.jar"
    curl -fL -o "$LIB/midp-2.0-stub.jar" \
        "https://repo1.maven.org/maven2/com/github/mcpat/apistubs/midp-2.0-stub/1.0/midp-2.0-stub-1.0.jar"
fi

PROGUARD="$TOOLS/proguard.jar"
if [ ! -f "$PROGUARD" ]; then
    echo "Downloading ProGuard 6.3 ..."
    curl -fL -o "$PROGUARD" \
        "https://repo1.maven.org/maven2/net/sf/proguard/proguard-base/6.3.0beta1/proguard-base-6.3.0beta1.jar"
fi

# --------------------------------------------------------------- tests ---
if [ "${1:-}" = "test" ]; then
    echo "Running date tests ..."
    rm -rf "$BUILD/test-classes"
    mkdir -p "$BUILD/test-classes"
    "$JDK/bin/javac" -encoding UTF-8 -d "$BUILD/test-classes" \
        src/com/safaksayar/DateUtil.java test/DateUtilTest.java
    "$JDK/bin/java" -cp "$BUILD/test-classes" com.safaksayar.DateUtilTest
fi

# --------------------------------------------------------------- build ---
echo "Compiling ..."
rm -rf "$BUILD/classes" "$BUILD/preverified.jar"
mkdir -p "$BUILD/classes"
find src -name '*.java' > "$BUILD/sources.txt"
"$JDK/bin/javac" -encoding UTF-8 -source 1.4 -target 1.4 -nowarn \
    -bootclasspath "$LIB/cldc-1.1-stub.jar:$LIB/midp-2.0-stub.jar" \
    -d "$BUILD/classes" @"$BUILD/sources.txt"
cp res/icon.png "$BUILD/classes/icon.png"

echo "Preverifying (CLDC StackMap) ..."
"$JDK/bin/java" -jar "$PROGUARD" @proguard.pro

echo "Packaging ..."
rm -rf "$BUILD/final"
mkdir -p "$BUILD/final"
(
    cd "$BUILD/final"
    "$JDK/bin/jar" xf "$BUILD/preverified.jar"
)
rm -rf "$BUILD/final/META-INF"
"$JDK/bin/jar" cfm "$DIST/$APP_NAME.jar" res/MANIFEST.MF \
    -C "$BUILD/final" .

JAR_SIZE="$(wc -c < "$DIST/$APP_NAME.jar" | tr -d '[:space:]')"
{
    printf 'MIDlet-1: Safak Sayar, /icon.png, %s\r\n' "$MAIN_CLASS"
    printf 'MIDlet-Name: Safak Sayar\r\n'
    printf 'MIDlet-Version: 1.0.1\r\n'
    printf 'MIDlet-Vendor: Asker Safak Sayar\r\n'
    printf 'MIDlet-Icon: /icon.png\r\n'
    printf 'MIDlet-Jar-URL: %s.jar\r\n' "$APP_NAME"
    printf 'MIDlet-Jar-Size: %s\r\n' "$JAR_SIZE"
    printf 'MicroEdition-Configuration: CLDC-1.1\r\n'
    printf 'MicroEdition-Profile: MIDP-2.0\r\n'
} > "$DIST/$APP_NAME.jad"

# --------------------------------------------------------------- check ---
MAJOR="$("$JDK/bin/javap" -classpath "$DIST/$APP_NAME.jar" -verbose "$MAIN_CLASS" \
    2>/dev/null | awk '/major version/ {print $3; exit}')"
if [ "$MAJOR" != "48" ]; then
    echo "Unexpected class file version: $MAJOR (expected 48 / Java 1.4)" >&2
    exit 1
fi
if ! "$JDK/bin/javap" -classpath "$DIST/$APP_NAME.jar" -verbose "$MAIN_CLASS" \
        2>/dev/null | grep -q 'StackMap'; then
    echo "Warning: no CLDC StackMap attribute found; preverification may have failed." >&2
fi

echo
echo "Built:"
echo "  $DIST/$APP_NAME.jar  ($JAR_SIZE bytes, class version 48)"
echo "  $DIST/$APP_NAME.jad"
echo
echo "Copy both files to the phone (same folder) and open the .jad file."
