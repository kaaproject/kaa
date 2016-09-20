#!/bin/sh

docker-compose -p usingcompose exec kaa cat /var/log/kaa/* | grep ERROR
