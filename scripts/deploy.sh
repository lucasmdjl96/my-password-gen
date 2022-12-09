#!/bin/bash
#
# This file is part of MyPasswordGen.
#
# MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
#
# MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
#

set -Eueo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"/..

# shellcheck disable=SC2046
export $(xargs < ./secrets/deploy.env)

mkdir -p ~/.ssh
touch ~/.ssh/known_hosts
if ! grep -q "$DEPLOY_HOST" ~/.ssh/known_hosts; then
  ssh-keyscan -t rsa "$DEPLOY_HOST" >> ~/.ssh/known_hosts
fi
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

chmod 600 "$DEPLOY_KEY_PATH"

scp -i "$DEPLOY_KEY_PATH" "$LAUNCH_ENV_SRC.copy" "$DEPLOY_USER@$DEPLOY_HOST:$LAUNCH_ENV_TARGET"
scp -i "$DEPLOY_KEY_PATH" "$LAUNCH_SCRIPT_SRC.copy" "$DEPLOY_USER@$DEPLOY_HOST:$LAUNCH_SCRIPT_TARGET"
scp -i "$DEPLOY_KEY_PATH" "$JAR_SRC" "$DEPLOY_USER@$DEPLOY_HOST:$JAR_TARGET_PARENT/$JAR_TARGET_NAME"
scp -i "$DEPLOY_KEY_PATH" "$KEY_STORE_SRC" "$DEPLOY_USER@$DEPLOY_HOST:$KEY_STORE_TARGET"

rm "$LAUNCH_ENV_SRC.copy"
rm "$LAUNCH_SCRIPT_SRC.copy"

ssh -i "$DEPLOY_KEY_PATH" "$DEPLOY_USER@$DEPLOY_HOST" "sudo systemctl restart my-password-gen"
