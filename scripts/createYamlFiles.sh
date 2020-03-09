#!/bin/bash

YAMLDIR="/home/antru/workspace/semagrow-geotools/src/main/resources/deploy"

WORKDIR=$1
WORKID=$2

if [ -z $WORKDIR ] || [ -z $WORKID ]
then
  echo "Usage:"
  echo "$0 [workdir] [workid]"
  exit
fi

cd $WORKDIR
mkdir deploy

for DIR in `ls -l | grep ^d | awk '{ print $9 }'`
do
  cat $YAMLDIR/invekos.yaml | sed "s/\/inv\//$DIR/g" > "deploy/$DIR.yaml"
done

cat $YAMLDIR/semagrow.yaml | sed "s/\/sg\//semagrow-$WORKID/g" > "deploy/semagrow-$WORKID.yaml"