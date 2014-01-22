#!/bin/bash

cat *sql > /tmp/fam.sql
mysql -u ${db.username} -p ${db.name} < /tmp/fam.sql
