#!/bin/bash

set -e

usage() 
{
    echo "Usage: $0 [type]"
    exit 1
}

BUILD_TYPE="release"

if [ "$1" ]; then
    BUILD_TYPE="$1"
fi

if [ -z "${BUILD_TYPE}" ]; then
    usage
fi

./gradlew installDist -PbuildVariant="${BUILD_TYPE}"
