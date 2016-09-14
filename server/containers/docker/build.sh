#!/bin/sh

docker build --build-arg setupfile=kaa-node.deb -t kaa-node:0.10.0 .
