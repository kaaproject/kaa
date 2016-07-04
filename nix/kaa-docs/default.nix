#
#  Copyright 2016 CyberVision, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

{ stdenv
, bundlerEnv
, git
, ruby
}:

let
  ruby-deps = bundlerEnv {
    name = "kaa-docs-ruby-deps";

    # The only file that should be edited by hand is ./Gemfile.
    # To get a Gemfile.lock, run nix-shell -p bundler --run 'bundler lock'
    # To get a gemset.nix, run nix-shell -p bundix --run 'bundix'
    # You can update both in one go:
    # nix-shell -p bundler -p bundix --run 'bundler lock && bundix'
    gemfile = ./Gemfile;
    lockfile = ./Gemfile.lock;
    gemset = ./gemset.nix;
  };

in stdenv.mkDerivation {
  name = "kaa-docs";
  buildInputs = [ git ruby ruby-deps ];
}
