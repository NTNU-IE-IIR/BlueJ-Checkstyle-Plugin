#!/usr/bin/env zsh
#
# Builds the plugin, installs it into BlueJ, and starts BlueJ (macOS) with a
# JDWP debug agent so a debugger (e.g. IntelliJ "Remote JVM Debug") can attach.
#
# Usage:
#   ./debugInBlueJ.sh [--suspend] [--no-build] [--port <port>]
#
#   --suspend    Wait for the debugger to attach before BlueJ starts, so that
#                breakpoints in CheckstyleExtension.startup() are hit.
#   --no-build   Skip "mvn clean package" and install the jar already in target/.
#   --port       Debug port to listen on (default: 5005).
#
# Environment overrides:
#   BLUEJ_APP        Path to BlueJ.app (default: /Applications/BlueJ.app)
#   BLUEJ_EXT_DIR    extensions2 directory to install into
#                    (default: $BLUEJ_APP/Contents/Java/extensions2)
#   BLUEJ_JAVA_HOME  JDK used to run BlueJ (default: /usr/libexec/java_home -v 21)

set -euo pipefail

SCRIPT_NAME="${0:t}"
SCRIPT_DIR="${0:A:h}"
PROJECT_DIR="${SCRIPT_DIR:h}"

usage() {
  echo "Usage: $SCRIPT_NAME [--suspend] [--no-build] [--port <port>]"
}

SUSPEND="n"
BUILD=true
PORT=5005

while [[ $# -gt 0 ]]; do
  case "$1" in
    --suspend)  SUSPEND="y"; shift ;;
    --no-build) BUILD=false; shift ;;
    --port)     PORT="${2:?--port requires a value}"; shift 2 ;;
    -h|--help)  usage; exit 0 ;;
    *)          echo "Error: unknown option $1" >&2; usage; exit 1 ;;
  esac
done

BLUEJ_APP="${BLUEJ_APP:-/Applications/BlueJ.app}"
BLUEJ_LIB="$BLUEJ_APP/Contents/Java"
BLUEJ_EXT_DIR="${BLUEJ_EXT_DIR:-$BLUEJ_LIB/extensions2}"
BLUEJ_JAVA_HOME="${BLUEJ_JAVA_HOME:-$(/usr/libexec/java_home -v 21)}"

if [[ ! -f "$BLUEJ_LIB/boot.jar" ]]; then
  echo "Error: could not find $BLUEJ_LIB/boot.jar. Set BLUEJ_APP to your BlueJ.app." >&2
  exit 1
fi

# 1. Build
if $BUILD; then
  echo "Building plugin..."
  (cd "$PROJECT_DIR" && mvn -q clean package)
fi

# The shaded jar is the one without the -original suffix.
JARS=("$PROJECT_DIR"/target/checkstyle4bluej-*.jar(N))
JARS=(${JARS:#*-original.jar})
if [[ ${#JARS} -ne 1 ]]; then
  echo "Error: expected exactly one shaded jar in target/, found ${#JARS}. Build first." >&2
  exit 1
fi
PLUGIN_JAR="${JARS[1]}"

# 2. Install (remove older checkstyle4bluej versions so BlueJ doesn't load two)
mkdir -p "$BLUEJ_EXT_DIR"
rm -f "$BLUEJ_EXT_DIR"/checkstyle4bluej-*.jar(N)
cp "$PLUGIN_JAR" "$BLUEJ_EXT_DIR/"
echo "Installed ${PLUGIN_JAR:t} into $BLUEJ_EXT_DIR"

# 3. Run BlueJ with the debug agent.
# JavaFX must be on the classpath (not the module path), or BlueJ fails with
# an IllegalAccessError on com.sun.glass.ui.
CLASSPATH_JARS=("$BLUEJ_LIB/boot.jar" "$BLUEJ_LIB"/javafx-*.jar)

echo "Starting BlueJ with JDWP on localhost:$PORT (suspend=$SUSPEND)"
echo "Attach with IntelliJ: Run > Edit Configurations > Remote JVM Debug, localhost:$PORT"
exec "$BLUEJ_JAVA_HOME/bin/java" \
  "-agentlib:jdwp=transport=dt_socket,server=y,suspend=$SUSPEND,address=localhost:$PORT" \
  -Dapple.laf.useScreenMenuBar=true \
  -cp "${(j.:.)CLASSPATH_JARS}" \
  bluej.Boot
