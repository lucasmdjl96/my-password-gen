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

apt-get -qq update
apt-get -qq -y --no-install-recommends install git gnupg apt-transport-https wget
echo 'deb https://gitsecret.jfrog.io/artifactory/git-secret-deb git-secret main' >> /etc/apt/sources.list
wget -qO - 'https://gitsecret.jfrog.io/artifactory/api/gpg/key/public' | apt-key add -
apt-get -qq update
apt-get install -qq -y --no-install-recommends git-secret
cd "$JB_SPACE_WORK_DIR_PATH"
echo "$GPG_PRIVATE_KEY" > private_key.gpg
gpg --batch --yes --pinentry-mode loopback --import private_key.gpg
git secret reveal -p "$GPG_PASSPHRASE"
./gradlew deploy -x check
