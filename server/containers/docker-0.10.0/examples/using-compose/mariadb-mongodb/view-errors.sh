#!/bin/sh

docker-compose -p kaaiot exec kaa cat /var/log/kaa/* | grep ERROR
