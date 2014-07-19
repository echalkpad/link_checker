#!/bin/bash
set -e
rm -rf scraper
make
sudo docker build -t eogren/scraper .
