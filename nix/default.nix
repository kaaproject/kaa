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

{ system ? builtins.currentSystem
, pkgs ? import <nixpkgs> { inherit system; }
}:

let
  callPackage = pkgs.lib.callPackageWith (pkgs // self);

  self = rec {
    gcc-xtensa-lx106 = callPackage ./gcc-xtensa-lx106 { };

    esp8266-rtos-sdk = callPackage ./esp8266-rtos-sdk { };

    cc3200-sdk = callPackage ./cc3200-sdk { };

    raspberrypi-tools = callPackage ./raspberrypi-tools { };

    raspberrypi-openssl = callPackage ./raspberrypi-openssl { };

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

    kaa-client-c = callPackage ./kaa-client-c { };

    kaa-client-cpp = callPackage ./kaa-client-cpp { };

    kaa-docs = callPackage ./kaa-docs { };
  };

in self
