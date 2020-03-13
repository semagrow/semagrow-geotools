FROM xqdocker/ubuntu-openjdk:jdk-8

MAINTAINER Antonis Troumpoukis <antru@iit.demokritos.gr>

WORKDIR /

RUN apt-get update && \
    apt-get install -y git && \
    apt-get install -y maven --allow-unauthenticated && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN git clone https://github.com/semagrow/semagrow-geotools.git && \
    cd semagrow-geotools && \
    mvn clean package

ADD run-experiments.sh /run-experiments.sh

ENTRYPOINT ["/bin/bash", "/run-experiments.sh"]