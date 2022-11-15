#!/bin/bash
set -Eueo pipefail

apt-get -qq update
apt-get -qq -y --no-install-recommends install git gnupg apt-transport-https wget
echo 'deb https://gitsecret.jfrog.io/artifactory/git-secret-deb git-secret main' >> /etc/apt/sources.list
wget -qO - 'https://gitsecret.jfrog.io/artifactory/api/gpg/key/public' | apt-key add -
apt-get -qq update
apt-get install -qq -y --no-install-recommends git-secret
cd ${'$'}JB_SPACE_WORK_DIR_PATH
echo "${'$'}GPG_PRIVATE_KEY" > ./private_key.gpg
gpg --batch --yes --pinentry-mode loopback --import private_key.gpg
git secret reveal -p "${'$'}GPG_PASSPHRASE"
./gradlew deploy
