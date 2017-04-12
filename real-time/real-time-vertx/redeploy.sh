#!/usr/bin/env bash

set -e -x

APP=real-time-vertx

mvn clean package -DskipTests=true; oc start-build ${APP} --from-dir=. --follow
