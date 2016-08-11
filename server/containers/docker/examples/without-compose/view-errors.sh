#!/bin/sh

docker exec kaa cat /var/log/kaa/* | grep ERROR