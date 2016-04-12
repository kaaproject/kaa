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

{ stdenv, wine, requireFile, xvfb_run
}:
stdenv.mkDerivation rec {

  name = "cc3200-sdk-1.1.0";

  src = requireFile {
    name = "CC3200SDK-1.1.0-windows-installer.exe";
    sha256 = "1m5yjp2wjpgbjkq200b700gmz974519fw7rxby8cfxirkzknicw1";
    url = "http://www.ti.com/tool/cc3200sdk";
  };

  nativeBuildInputs = [ xvfb_run wine ];

  unpackPhase = ":";

  dontStrip = true;

  installPhase = ''
    mkdir -p $out/lib/cc3200-sdk
    WINEPREFIX=$PWD/.wine xvfb-run wine ${src} --mode unattended --prefix $out/lib/cc3200-sdk/
  '';
}
