#!/usr/bin/env bash

set -e -x

oc get configmaps -o name | xargs oc delete
oc delete all --all
