#!/bin/bash

cat *sql > /tmp/fam.sql
mysql ${db.name} -u ${db.username} -p ${db.password} < /tmp/fam.sql
