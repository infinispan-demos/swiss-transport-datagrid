#!/bin/bash

set -e -x

(cd ../analytics; mvn clean package -pl analytics-server -am)
rm -drf target
mkdir target
cp ../analytics/analytics-server/target/analytics-server-1.0-SNAPSHOT.jar target/analytics-server.jar

oc policy add-role-to-user view system:serviceaccount:myproject:default -n myproject
oc create configmap server-configuration --from-file=./server-config.xml
oc create -f ./datagrid.yml
