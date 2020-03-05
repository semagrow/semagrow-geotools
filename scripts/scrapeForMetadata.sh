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
  $SEVOD_SCRAPER geordfdump $FILE $ENDPOINT $PREFIXES /dev/null /dev/null $FILE.ttl
done

FNAME="metadata.ttl"

rm $FNAME
touch $FNAME

cat $DATASET.*.ttl | grep "^@prefix" | sort | uniq >> $FNAME
cat $DATASET.*.ttl | grep "rdfs:subPropertyOf" | sort | uniq >> $FNAME
cat $DATASET.*.ttl | grep -v "^@prefix" | grep -v "rdfs:subPropertyOf" >> $FNAME

rm $DATASET.*.ttl
