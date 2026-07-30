#!/usr/bin/env bash

./dhis-2/build.sh
IMAGE_REPOSITORY="udsmdhis2/dhis2custom" BASE_IMAGE="dhis2/core:2.43.1-rc" GIT_COMMIT=$(git rev-parse HEAD) GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD) DHIS2_VERSION="2.43.1" ./dhis-2/build-docker-image.sh -t 43.1