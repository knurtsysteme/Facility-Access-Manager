#!/bin/bash

# update couchdb
#
# @since 07/12/2012
# @version 07/12/2012
# @author info@knurt.de

JSON_FILE="./as.json";
DESIGN_URI="_design/as";

if test ! -f $JSON_FILE; then
  echo "FAIL could not find $JSON_FILE! Make sure file is in same directory";
  exit 1
fi

read -p "CouchDB Host [127.0.0.1]: " DB_HOST
DB_HOST=${DB_HOST:-127.0.0.1}

read -p "CouchDB Port [5984]: " DB_PORT
DB_PORT=${DB_PORT:-5984}

read -p "CouchDB Name [fam]: " DB_NAME
DB_NAME=${DB_NAME:-fam}

read -p "CouchDB User [admin]: " DB_USER
DB_USER=${DB_USER:-admin}

read -sp "CouchDB Password [secret]: " DB_PASS
DB_PASS=${DB_PASS:-secret}

ADMIN_URL="http://"$DB_USER":"$DB_PASS"@"$DB_HOST":"$DB_PORT"/"$DB_NAME

DB_EXISTS=1
if test `curl $ADMIN_URL 2>/dev/null | grep "no_db_file" | wc -l` -eq 1; then
  DB_EXISTS=0
fi

if test $DB_EXISTS -eq 0; then
  echo "Creating database "$DB_NAME
  curl -X PUT $ADMIN_URL
else
  REV=`curl $ADMIN_URL"/"$DESIGN_URI 2>/dev/null`
  REV=`echo $REV | sed "s/.*rev\":\"//g" | sed "s/\".*//g"`
  echo -e "\nPlease accept deleting /"$DB_NAME"/"$DESIGN_URI"?rev="$REV"!"
  read -p "Type ok: " OK
  OK=${OK:-no}
  if test $OK == "ok"; then
    curl -X DELETE $ADMIN_URL"/"$DESIGN_URI"?rev="$REV
  else
    echo "FAIL: Delete old design document first"
    exit 3
  fi
fi

curl -X PUT -d @$JSON_FILE $ADMIN_URL"/"$DESIGN_URI;