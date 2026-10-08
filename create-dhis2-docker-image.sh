#!/usr/bin/env bash

cd dhis-2 
./build.sh
cd ..
IMAGE_REPOSITORY="udsmdhis2/dhis2custom" BASE_IMAGE="dhis2/core:2.43.2" GIT_COMMIT=$(git rev-parse HEAD) GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD) DHIS2_VERSION="2.43.2" ./dhis-2/build-docker-image.sh -t 43.2.0