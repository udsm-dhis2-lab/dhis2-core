#!/usr/bin/env bash

./dhis-2/build.sh
IMAGE_REPOSITORY="udsmdhis2/dhis2custom" BASE_IMAGE="dhis2/core:2.40.9.1" GIT_COMMIT=$(git rev-parse HEAD) GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD) DHIS2_VERSION="2.40.9.1" ./dhis-2/build-docker-image.sh -t 40.9.1