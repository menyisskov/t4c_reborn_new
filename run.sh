#!/usr/bin/env bash
# Builds the runtime classpath (if needed) and launches the T4C Reborn game client.
set -e

cd "$(dirname "$0")"

CP_FILE="target/classpath.txt"

if [ ! -f "$CP_FILE" ]; then
  echo "Building dependency classpath..."
  mvn -q dependency:build-classpath -Dmdep.outputFile="$CP_FILE"
fi

java -cp "target/classes;target/natives;$(cat "$CP_FILE")" com.perso.T4C.MyGame
