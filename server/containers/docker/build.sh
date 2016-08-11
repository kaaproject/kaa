#!/bin/sh

docker build --build-arg setupfile=install/kaa-node.deb -t kaaproject/kaa:0.9.0 .
