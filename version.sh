#!/bin/bash
cd build/processedResources/jvm/main
hash=$(cat \
html/index.html \
static/css/style.css \
static/js/my-password-gen.js \
| sha256sum | cut -d ' ' -f1)
sed -i "s/\$VERSION/${hash:0:16}/g" static/js/service-worker.js
cd ../../../..
