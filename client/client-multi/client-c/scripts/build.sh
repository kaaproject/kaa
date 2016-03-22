#! /bin/sh

set -e
nix-shell $@ --pure --run "make"
nix-shell $@ --pure --run "./build.sh test"
