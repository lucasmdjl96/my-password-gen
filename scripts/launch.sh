#!/bin/bash
set -Eueo pipefail

export "$(xargs < "$ENV_FILE")"

cd "$JAR_PARENT"
rm -fr jars
mkdir jars
mv "$JAR_NAME" jars/"$JAR_NAME"
java -jar jars/"$JAR_NAME"
