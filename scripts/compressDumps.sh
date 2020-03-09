#!/bin/bash

WORKDIR=$1
WORKID=$2

if [ -z $WORKDIR ] || [ -z $WORKID ]
then
  echo "Usage:"
  echo "$0 [workdir] [workid]"
  exit
fi

cd $WORKDIR

for FILE in *.nt
do
  BASE=`echo $FILE | sed "s/[a-zA-Z]*\.\([0-9]*\)\.nt/$WORKID-\1/g"`
  mkdir $BASE
  cp $FILE $BASE/$FILE
  tar czvf $BASE.tar.gz $BASE
done
