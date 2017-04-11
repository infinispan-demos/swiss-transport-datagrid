#!/bin/bash

set -e -x

oc policy add-role-to-user view system:serviceaccount:myproject:default -n myproject || true
oc create configmap server-configuration --from-file=./server-config.xml  || true
oc create -f ./datagrid.yml || true
