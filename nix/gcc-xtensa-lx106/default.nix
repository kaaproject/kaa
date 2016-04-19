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

{ stdenv, fetchurl, zlib
}:
let
  src =
    if stdenv.system == "i686-linux" then
       fetchurl {
         url = "http://arduino.esp8266.com/linux32-xtensa-lx106-elf.tar.gz";
         sha256 = "0nlmvjfjv3mcj0ihvf27dflrmjhads00wr2si42zny00ky0ifj5j";
       } else
    if stdenv.system == "x86_64-linux" then
       fetchurl {
         url = http://arduino.esp8266.com/linux64-xtensa-lx106-elf-gb404fb9.tar.gz;
         sha256 = "1dvk2i1r5nw9ajlv4h4rs2b2149qa0rayz8n4sd8h85kv3xmgw26";
       } else
    if stdenv.system == "i686-darwin" || stdenv.system == "x86_64-darwin" then
       fetchurl {
         url = http://arduino.esp8266.com/osx-xtensa-lx106-elf-gb404fb9-2.tar.gz;
         sha256 = "1rp4p5b9wqddm8gfw6mwax906c0pf54kv7glw1ai7gcp74cm1w8c";
       } else
    abort "no snapshot for this platform (missing target triple)";

in stdenv.mkDerivation {
  name = "gcc-xtensa-lx106-1.20.0-26-gb404fb9-2";

  inherit src;

  installPhase = ''
    mkdir -p $out
    cp -r . $out
  '';

  preFixup = if stdenv.isLinux then let
    rpath = stdenv.lib.concatStringsSep ":" [
      "$out/lib"
      (stdenv.lib.makeLibraryPath [ zlib stdenv.cc.cc ])
    ];
  in ''
    find -H $out/ -type f -executable -exec \
      patchelf \
        --set-interpreter "${stdenv.glibc.out}/lib/${stdenv.cc.dynamicLinker}" \
        --set-rpath "${rpath}" \
        {} \;
  '' else "";

  dontStrip = true;
}
