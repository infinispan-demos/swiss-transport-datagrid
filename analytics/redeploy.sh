#!/usr/bin/env bash

set -e -x

APP=analytics

mvn clean package -pl analytics-client -am; oc start-build ${APP} --from-dir=. --follow
