#!/bin/bash

FILE=$1

if [ -z $FILE ]
then
  echo "Usage:"
  echo "$0 [metadata_file]"
  exit
fi

echo -n "GEOMETRYCOLLECTION("
grep 'POLYGON' $FILE \
  | cut -f1 -d'>' --complement \
  | colrm 1 1 \
  | cut -f1 -d\" \
  | tr '\n' , \
  | rev | colrm 1 1 | rev
echo ")"
