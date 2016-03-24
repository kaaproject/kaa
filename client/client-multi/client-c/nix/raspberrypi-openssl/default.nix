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

{ stdenv, fetchurl, raspberrypi-tools, perl
}:
stdenv.mkDerivation {
  name = "raspberrypi-openssl-1.0.2g";

  src = fetchurl {
    url = "ftp://ftp.openssl.org/source/openssl-1.0.2g.tar.gz";
    sha256 = "0cxajjayi859czi545ddafi24m9nwsnjsw4q82zrmqvwj2rv315p";
  };

  nativeBuildInputs = [ perl raspberrypi-tools ];

  preConfigure = ''
    unset CC
    unset CXX
    configureFlagsArray=("shared" "os/compiler:arm-linux-gnueabihf-gcc")
  '';

  configureScript = "./Configure";

  installTargets = "install_sw";

  dontStrip = true;
}
