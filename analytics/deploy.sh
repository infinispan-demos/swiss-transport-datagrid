#!/usr/bin/env bash

set -e -x

APP=analytics

oc new-build --binary --name=${APP} -l app=${APP}
mvn clean package -pl analytics-client -am; oc start-build ${APP} --from-dir=. --follow
oc new-app ${APP} -l app=${APP},hystrix.enabled=true
oc expose service ${APP}
