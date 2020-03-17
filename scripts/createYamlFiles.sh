#!/bin/bash

YAMLDIR="/home/antru/workspace/semagrow-geotools/src/main/resources/deploy"

WORKDIR=$1
ID=$2
NUM=`expr $3 - 1`

if [ -z $WORKDIR ] || [ -z $ID ] || [ -z $NUM ]
then
  echo "Usage:"
  echo "$0 [workdir] [workid] [datasetnum]"
  exit
fi

cd $WORKDIR
mkdir deploy

for I in `seq 0 $NUM`
do
  cat $YAMLDIR/invekos.yaml | sed "s/\/id\//$ID-$I/g" > "deploy/invekos-$ID-$I.yaml"
done

cat $YAMLDIR/semagrow.yaml | sed "s/\/id\//$ID/g" > "deploy/semagrow-$ID.yaml"

cat $YAMLDIR/exp-sim.yaml | sed "s/\/id\//$ID/g" > "deploy/exp-sim-$ID.yaml"
cat $YAMLDIR/exp-thm.yaml | sed "s/\/id\//$ID/g" > "deploy/exp-thm-$ID.yaml"