#!/bin/bash

cd /semagrow-geotools/scripts

touch endpoints.txt

for i in $(echo $STRABON_ENDPOINTS | sed "s/,/ /g")
do
    echo "http://$i.default.svc.cluster.local:8080/strabon/Query" >> endpoints.txt
done

for i in $(echo $SEMAGROW_ENDPOINTS | sed "s/,/ /g")
do
    echo "http://$i.default.svc.cluster.local:8080/SemaGrow/sparql" >> endpoints.txt
done

for i in `seq -w $TIMES_TO_RUN`
do
  ./runExperiment.sh endpoints.txt results.$i.csv
done

for i in `seq -w $TIMES_TO_RUN`
do
  echo
  cat results.$i.csv
done

