#!/bin/bash

SEVOD_SCRAPER="/home/antru/workspace/sevod-scraper/assembly/target/bin/sevod-scraper.sh"
PREFIXESDIR="/home/antru/workspace/semagrow-geotools/src/main/resources/"
DATASET="dump"
WORKDIR=$1

if [ -z $WORKDIR ]
then
  echo "Usage:"
  echo "$0 [workdir]"
  exit
fi

cd $WORKDIR

for FILE in $DATASET.*.nt
do
  I=`echo $FILE | sed 's/dump.//g' | sed 's/.nt//g'`
  ENDPOINT="http://invekos-$I.default.svc.cluster.local:8080/strabon/Query"
  PREFIXES="$PREFIXESDIR/invekosPrefixes.txt"
  $SEVOD_SCRAPER geordfdump $FILE $ENDPOINT $PREFIXES $FILE.ttl
done

FNAME="metadata.ttl"

rm $FNAME
touch $FNAME

cat $DATASET.*.nt.ttl | grep "^@prefix" | sort | uniq >> $FNAME
cat $DATASET.*.nt.ttl | grep -v "^@prefix" >> $FNAME

rm $DATASET.*.nt.ttl
