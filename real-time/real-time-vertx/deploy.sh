#!/usr/bin/env bash

set -e -x

APP=real-time-vertx

oc new-build --binary --name=${APP} -l app=${APP}
mvn clean package -DskipTests=true; oc start-build ${APP} --from-dir=. --follow
oc new-app ${APP} -l app=${APP},hystrix.enabled=true
oc expose service ${APP}
