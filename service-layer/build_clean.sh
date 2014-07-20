#!/bin/bash
set -e
rm -rf target
mvn package
sudo docker build -t eogren/service-layer .
