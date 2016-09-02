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

  nixpkgs-16_03 = import (nixpkgs-bootstrap.fetchFromGitHub {
    owner = "NixOS";
    repo = "nixpkgs-channels";
    rev = "baf46b99e33005348fdbd083366c330be4b373f3";
    sha256 = "19wq2ayn9l5qd2s6s07sjh49kc6qlpadyy098zzayxj6nprvwzmb";
  }) { };

in

{ pkgs ? nixpkgs-16_03
, pkgs-tools ? nixpkgs-bootstrap
}:

let
  # We want to use latest versions of tools we have available
  # (more checks, less false positives)
  tools = {
    doxygen = pkgs-tools.doxygen;
    valgrind = pkgs-tools.valgrind;
    cppcheck = pkgs-tools.cppcheck;
  };

  callPackage = pkgs.lib.callPackageWith (pkgs // tools // self);

  self = rec {
    avro-cpp = callPackage ./avro-c++ { };

    gcc-xtensa-lx106 = callPackage ./gcc-xtensa-lx106 { };

    esp8266-rtos-sdk = callPackage ./esp8266-rtos-sdk { };

    cc3200-sdk = callPackage ./cc3200-sdk { };

    raspberrypi-tools = callPackage ./raspberrypi-tools { };

    # Currently, it causes compilation failure, so we use 4.7 for now.
    # gcc-arm-embedded = pkgs.gcc-arm-embedded-5_2;
    gcc-arm-embedded = pkgs.gcc-arm-embedded-4_7;

    # Submitted patch upstream:
    # https://sourceforge.net/p/astyle/bugs/396/
    astyle = pkgs.astyle.overrideDerivation (self: {
      sourceRoot = "astyle";
      preBuild = ''
        cd build/${if self.stdenv.cc.isClang then "clang" else "gcc"}
      '';
      patches = [ ./astyle/max_indent.patch ];
    });

    kaa-client-c = callPackage ./kaa-client-c { cmake = pkgs.cmake-2_8; };

    kaa-client-cpp = callPackage ./kaa-client-cpp { cmake = pkgs.cmake-2_8; };

    kaa-docs = callPackage ./kaa-docs { };
  };

in self
