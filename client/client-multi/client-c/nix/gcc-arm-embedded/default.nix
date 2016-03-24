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

{ stdenv, bzip2, patchelf, glibc, gcc, fetchurl, version, releaseType, sha256, ncurses
, dirName ? null, subdirName ? null }:
with stdenv.lib;
let
  versionParts = splitString "-" version; # 4.7 2013q3 20130916
  majorVersion = elemAt versionParts 0; # 4.7
  yearQuarter = elemAt versionParts 1; # 2013q3
  underscoreVersion = replaceChars ["."] ["_"] version; # 4_7-2013q3-20130916
  yearQuarterParts = splitString "q" yearQuarter; # 2013 3
  year = elemAt yearQuarterParts 0; # 2013
  quarter = elemAt yearQuarterParts 1; # 3
  dirName_ = if dirName != null then dirName else majorVersion;
  subdirName_ = if subdirName != null then subdirName
    else "${majorVersion}-${year}-q${quarter}-${releaseType}"; # 4.7-2013-q3-update
in
stdenv.mkDerivation {
  name = "gcc-arm-embedded-${version}";

  src = fetchurl {
    url = "https://launchpad.net/gcc-arm-embedded/${dirName_}/${subdirName_}/+download/gcc-arm-none-eabi-${underscoreVersion}-linux.tar.bz2";
    sha256 = sha256;
  };

  buildInputs = [ bzip2 patchelf ];
 
  dontPatchELF = true;
  
  phases = "unpackPhase patchPhase installPhase";
  
  installPhase = ''
    mkdir -pv $out
    cp -r ./* $out

    for f in $(find $out); do
      if [ -f "$f" ] && patchelf "$f" 2> /dev/null; then
        patchelf --set-interpreter ${glibc}/lib/ld-linux.so.2 \
                 --set-rpath $out/lib:${gcc}/lib:${ncurses}/lib \
                 "$f" || true
      fi
    done
  '';

  meta = with stdenv.lib; {
    description = "Pre-built GNU toolchain from ARM Cortex-M & Cortex-R processors (Cortex-M0/M0+/M3/M4, Cortex-R4/R5/R7)";
    homepage = "https://launchpad.net/gcc-arm-embedded";
    license = licenses.gpl3;
    platforms = platforms.linux;
  };
}
