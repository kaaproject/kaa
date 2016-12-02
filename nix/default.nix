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

let
  nixpkgs-bootstrap = import <nixpkgs> { };

  # How to update nixpkgs version:
  # - go to https://github.com/NixOS/nixpkgs-channels
  # - select branch you want to track
  # - get latest commit hash -- this goes to `rev` field
  # - execute `nix-prefetch-url --unpack https://github.com/NixOS/nixpkgs-channels/archive/<rev>.tar.gz`
  # - output of the previous command goes to `sha256` field
  nixpkgs-16_09 = import (nixpkgs-bootstrap.fetchFromGitHub {
    owner = "NixOS";
    repo = "nixpkgs-channels";
    rev = "b833b10f81bee30c634802afbe06d9d0630c2709";
    sha256 = "01sdf85fb99vmfk2xnjm7ii7ac0vf112ywhkmi1v5dln1x2cxps7";
  }) { };

in

{ pkgs ? nixpkgs-16_09
, pkgs-tools ? nixpkgs-bootstrap
}:

let
  # We want to use latest versions of tools we have available
  # (more checks, less false positives)
  tools = {
    inherit (pkgs-tools)
      doxygen
      valgrind
      cppcheck;
  };

  callPackage = pkgs.lib.callPackageWith (pkgs // tools // self);

  self = rec {
    gcc-xtensa-lx106 = callPackage ./gcc-xtensa-lx106 { };

    esp8266-rtos-sdk = callPackage ./esp8266-rtos-sdk { };

    cc3200-sdk = callPackage ./cc3200-sdk { };

    raspberrypi-tools = callPackage ./raspberrypi-tools { };

    # Currently, it causes compilation failure, so we use 4.7 for now.
    # gcc-arm-embedded = pkgs.gcc-arm-embedded-5_2;
    gcc-arm-embedded = pkgs.gcc-arm-embedded-4_7;

    # Submitted patch upstream:
    # https://sourceforge.net/p/astyle/bugs/396/
    astyle = pkgs-tools.astyle.overrideDerivation (self: {
      patches = [ ./astyle/max_indent.patch ];
      patchFlags = "--directory=../.. -p1";
    });

    # cmake-2.8 doesn't work on Darwin
    test-cmake = if pkgs.stdenv.isDarwin then pkgs.cmake else pkgs.cmake_2_8;

    kaa-client-c = callPackage ./kaa-client-c { cmake = test-cmake; };

    kaa-client-cpp = callPackage ./kaa-client-cpp { cmake = test-cmake; };

    kaa-docs = callPackage ./kaa-docs { };
  };

in self
