#!/bin/bash
set -e
lein clean
lein uberjar
docker build -t bctest:0.1.0 .
