#!/bin/bash

mysql ${db.name} -u ${db.username} -p ${db.password} < *sql
