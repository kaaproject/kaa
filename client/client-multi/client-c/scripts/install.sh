#! /bin/sh

set -e
curl https://nixos.org/nix/install | sh
. $HOME/.nix-profile/etc/profile.d/nix.sh
sudo mkdir /etc/nix
sudo sh -c 'echo "build-max-jobs = 4" > /etc/nix/nix.conf'
nix-shell -Q $@ --pure --run true
