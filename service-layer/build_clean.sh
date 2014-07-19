#!/bin/bash
rm -rf target
mvn package
sudo docker build -t eogren/service_layer .
