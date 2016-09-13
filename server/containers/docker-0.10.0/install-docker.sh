#!/bin/sh
# Install docker and docker compose
    curl -sSL https://get.docker.com/ | sh
    sudo curl -L https://github.com/docker/compose/releases/download/1.5.1/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose