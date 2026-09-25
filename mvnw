#!/bin/sh
set -e

MAVEN_VERSION=3.9.9
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_DIR="$SCRIPT_DIR/.mvn/wrapper"
DIST_DIR="$WRAPPER_DIR/dists/apache-maven-$MAVEN_VERSION"
MAVEN_HOME="$DIST_DIR/apache-maven-$MAVEN_VERSION"
MVN_CMD="$MAVEN_HOME/bin/mvn"

if [ ! -x "$MVN_CMD" ]; then
    echo "Apache Maven $MAVEN_VERSION not found locally, downloading..."
    mkdir -p "$DIST_DIR"
    curl -fsSL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.zip" -o "$DIST_DIR/maven.zip"
    unzip -q -o "$DIST_DIR/maven.zip" -d "$DIST_DIR"
    rm "$DIST_DIR/maven.zip"
    chmod +x "$MVN_CMD"
fi

exec "$MVN_CMD" "$@"
