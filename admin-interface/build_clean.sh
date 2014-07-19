#!/bin/bash
rm -rf build
rm -rf dist
grunt dist
sudo docker build -t eogren/admin-interface .
