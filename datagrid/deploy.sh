#!/bin/bash

set -e -x

DEPLOY=target
#DEPLOY=/opt/deployments

rm -drf $DEPLOY
mkdir $DEPLOY

(cd ../analytics; mvn clean install -pl analytics-domain -am)
cp ../analytics/analytics-domain/target/analytics-domain-1.0-SNAPSHOT.jar $DEPLOY/analytics-domain.jar

(cd ../analytics; mvn clean install -pl analytics-server)
cp ../analytics/analytics-server/target/analytics-server-1.0-SNAPSHOT.jar $DEPLOY/analytics-server.jar

oc login -u system:admin
oc adm policy add-scc-to-user hostaccess system:serviceaccount:myproject:default -n myproject

oc login -u developer
oc policy add-role-to-user view system:serviceaccount:myproject:default -n myproject
oc create configmap server-configuration --from-file=./server-config.xml
oc create -f ./datagrid.yml
