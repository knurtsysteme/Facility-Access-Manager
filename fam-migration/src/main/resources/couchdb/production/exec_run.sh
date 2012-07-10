#!/bin/bash
#
# do:
#
# cd [...]/fam-migration
# mvn clean install
# chmod u+x ./target/classes/couchdb/production/run.sh
# cd ./target/classes/couchdb/production
# run.sh
NONE_ADMIN_URL=${dev.couchdb.default.url}/
ADMIN_URL=${dev.couchdb.admin.url}/

echo "# JUST ECHO USEFULL SCRIPTS HERE"
echo ""

echo "--------------------------------"
echo "# delete and create database (decomment first)"
echo "--------------------------------"
echo ""
echo "# curl -X DELETE $ADMIN_URL"
echo "# curl -X PUT $ADMIN_URL"
echo ""
echo "# update to last design document manually."
echo "# you MUST delete the old design document before to get this running!!!!."
echo ""
echo "--------------------------------"
echo "# migration of _design/as"
echo "--------------------------------"
echo ""
echo "# to delete the last revision:"
echo curl -X DELETE $ADMIN_URL"_design/as?rev="\`./exec_couchdb_getrev.sh $ADMIN_URL"_design/as"\`
echo "# PUT in _design/as.json"
echo curl -X PUT -d @_design/as.json $ADMIN_URL"_design/as";
echo ""
