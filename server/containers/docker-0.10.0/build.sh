#!/bin/sh

docker build --build-arg setupfile=install/kaa-node.deb -t cburr25/kaa:0.9.0 .
