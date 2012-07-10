#!/bin/bash
#
# return rev of document of url

REV=`curl -X GET $1 2>/dev/null`
echo $REV | sed "s/.*rev\":\"//g" | sed "s/\".*//g"
