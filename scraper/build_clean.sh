#!/bin/bash

rm -rf scraper
make
sudo docker build -t eogren/scraper .
