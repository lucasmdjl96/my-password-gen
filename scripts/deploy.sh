#!/bin/bash
set -Eueo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"/..

# shellcheck disable=SC2046
export $(xargs < ./secrets/deploy.env)
JAR_TARGET_NAME="my-password-gen-$DEPLOY_VERSION.jar"

cp "$LAUNCH_ENV_SRC" "$LAUNCH_ENV_SRC.copy"
{
  echo
  echo "KEY_STORE=$KEY_STORE_TARGET"
  echo "JAR_PARENT=$JAR_TARGET_PARENT"
  echo "JAR_NAME=$JAR_TARGET_NAME"
} >> "$LAUNCH_ENV_SRC.copy"

cp "$LAUNCH_SCRIPT_SRC" "$LAUNCH_SCRIPT_SRC.copy"
sed -i "s@\$ENV_FILE@$LAUNCH_ENV_TARGET@g" "$LAUNCH_SCRIPT_SRC.copy"

scp -i "$DEPLOY_KEY_PATH" "$LAUNCH_ENV_SRC.copy" "$DEPLOY_USER@$DEPLOY_HOST:$LAUNCH_ENV_TARGET"
scp -i "$DEPLOY_KEY_PATH" "$LAUNCH_SCRIPT_SRC.copy" "$DEPLOY_USER@$DEPLOY_HOST:$LAUNCH_SCRIPT_TARGET"
scp -i "$DEPLOY_KEY_PATH" "$JAR_SRC" "$DEPLOY_USER@$DEPLOY_HOST:$JAR_TARGET_PARENT/$JAR_TARGET_NAME"
scp -i "$DEPLOY_KEY_PATH" "$KEY_STORE_SRC" "$DEPLOY_USER@$DEPLOY_HOST:$KEY_STORE_TARGET"

rm "$LAUNCH_ENV_SRC.copy"
rm "$LAUNCH_SCRIPT_SRC.copy"

ssh -i "$DEPLOY_KEY_PATH" "$DEPLOY_USER@$DEPLOY_HOST" "sudo systemctl my-password-gen.system restart"
