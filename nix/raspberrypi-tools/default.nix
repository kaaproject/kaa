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

{ stdenv, fetchFromGitHub
}:
let
  toolchain-name =
    if stdenv.system == "i686-linux" then "gcc-linaro-arm-linux-gnueabihf-raspbian" else
    if stdenv.system == "x86_64-linux" then "gcc-linaro-arm-linux-gnueabihf-raspbian-x64" else
    abort "only Linux is supported";

in stdenv.mkDerivation {
  name = "raspberrypi-tools-20160311";

  src = fetchFromGitHub {
    owner = "raspberrypi";
    repo = "tools";
    rev = "3a413ca2b23fd275e8ddcc34f3f9fc3a4dbc723f";
    sha256 = "0qsgp34sb2nra0kyh8hbb9w91padqdhqjp7nsny96xh0xw24zwys";
  };

  dontPatchELF = true;

  phases = "unpackPhase patchPhase installPhase";

  installPhase = ''
    cp -ar arm-bcm2708/${toolchain-name}/ $out

    for f in $(find $out); do
      if [ -f "$f" ] && patchelf "$f" 2> /dev/null; then
        patchelf --set-interpreter "${stdenv.glibc.out or stdenv.glibc}/lib/${stdenv.cc.dynamicLinker}" \
                 "$f" || true
      fi
    done
  '';
}
