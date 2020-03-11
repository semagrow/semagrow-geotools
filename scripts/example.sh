#!/bin/bash

./partitionDataset.sh /home/antru/Documents/xearth/lucasdemo/dumps/invekos.nt /home/antru/workspace/semagrow-geotools/src/main/resources/AustriaMBB.txt 2 2 /home/antru/Documents/xearth/invekos/2x3
./partitionDataset.sh /home/antru/Documents/xearth/lucasdemo/dumps/invekos.nt /home/antru/workspace/semagrow-geotools/src/main/resources/AustriaMBB.txt 2 3 /home/antru/Documents/xearth/invekos/2x3
./partitionDataset.sh /home/antru/Documents/xearth/lucasdemo/dumps/invekos.nt /home/antru/workspace/semagrow-geotools/src/main/resources/AustriaMBB.txt 3 3 /home/antru/Documents/xearth/invekos/3x3
./partitionDataset.sh /home/antru/Documents/xearth/lucasdemo/dumps/invekos.nt /home/antru/workspace/semagrow-geotools/src/main/resources/AustriaMBB.txt 3 4 /home/antru/Documents/xearth/invekos/3x4
./partitionDataset.sh /home/antru/Documents/xearth/lucasdemo/dumps/invekos.nt /home/antru/workspace/semagrow-geotools/src/main/resources/AustriaMBB.txt 3 5 /home/antru/Documents/xearth/invekos/3x5
./partitionDataset.sh /home/antru/Documents/xearth/lucasdemo/dumps/invekos.nt /home/antru/workspace/semagrow-geotools/src/main/resources/AustriaMBB.txt 4 5 /home/antru/Documents/xearth/invekos/4x5
./partitionDataset.sh /home/antru/Documents/xearth/lucasdemo/dumps/invekos.nt /home/antru/workspace/semagrow-geotools/src/main/resources/AustriaMBB.txt 4 6 /home/antru/Documents/xearth/invekos/4x6
./partitionDatasetADM.sh /home/antru/Documents/xearth/lucasdemo/dumps/invekos.nt /home/antru/Documents/xearth/invekos/adm

./scrapeForMetadata.sh /home/antru/Documents/xearth/invekos/2x2
./scrapeForMetadata.sh /home/antru/Documents/xearth/invekos/2x3
./scrapeForMetadata.sh /home/antru/Documents/xearth/invekos/3x3
./scrapeForMetadata.sh /home/antru/Documents/xearth/invekos/3x4
./scrapeForMetadata.sh /home/antru/Documents/xearth/invekos/3x5
./scrapeForMetadata.sh /home/antru/Documents/xearth/invekos/4x5
./scrapeForMetadata.sh /home/antru/Documents/xearth/invekos/4x6

./compressDumps.sh /home/antru/Documents/xearth/invekos/2x2/ invekos-2x2
./compressDumps.sh /home/antru/Documents/xearth/invekos/2x3/ invekos-2x3
./compressDumps.sh /home/antru/Documents/xearth/invekos/3x3/ invekos-3x3
./compressDumps.sh /home/antru/Documents/xearth/invekos/3x4/ invekos-3x4
./compressDumps.sh /home/antru/Documents/xearth/invekos/3x5/ invekos-3x5
./compressDumps.sh /home/antru/Documents/xearth/invekos/4x5/ invekos-4x5
./compressDumps.sh /home/antru/Documents/xearth/invekos/4x6/ invekos-4x6

./createYamlFiles.sh /home/antru/Documents/xearth/invekos/2x2/ 2x2
./createYamlFiles.sh /home/antru/Documents/xearth/invekos/2x3/ 2x3
./createYamlFiles.sh /home/antru/Documents/xearth/invekos/3x3/ 3x3
./createYamlFiles.sh /home/antru/Documents/xearth/invekos/3x4/ 3x4
./createYamlFiles.sh /home/antru/Documents/xearth/invekos/3x5/ 3x5
./createYamlFiles.sh /home/antru/Documents/xearth/invekos/4x5/ 4x5
./createYamlFiles.sh /home/antru/Documents/xearth/invekos/4x6/ 4x6
