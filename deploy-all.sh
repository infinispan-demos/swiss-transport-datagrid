#!/usr/bin/env bash

set -e -x

(cd ./datagrid; ./deploy.sh)

(cd ./real-time/real-time-vertx; ./deploy.sh)
