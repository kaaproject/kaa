#!/bin/sh
# Install docker and docker compose
    wget -qO- https://get.docker.com/ | sh
    su -i
    curl -L https://github.com/docker/compose/releases/download/1.5.1/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    exit