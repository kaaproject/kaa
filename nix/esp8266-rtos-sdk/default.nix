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

{ stdenv, fetchurl, fetchFromGitHub
}:
let
  libhal = fetchurl {
    url = "https://github.com/esp8266/esp8266-wiki/raw/master/libs/libhal.a";
    sha256 = "0ai5m223cv6cp0jlzck4wrgrjasnjjkfbnabjhk1715j8s6n1a67";
  };
in stdenv.mkDerivation {
  name = "esp8266-rtos-sdk-20150626";

  src = fetchFromGitHub {
    owner = "espressif";
    repo = "ESP8266_RTOS_SDK";
    rev = "169a436ce10155015d056eab80345447bfdfade5";
    sha256 = "1zkszdvbv9rs0mx2pl0p4qivf22m4ck7wfh4zi093k5r5nsgd5dl";
  };

  patchPhase = ''
    sed -i "s/#include \"c_types.h\"/\/\/#include \"c_types.h\"/" include/lwip/arch/cc.h
  '';

  buildPhase = ":";

  installPhase = ''
    mkdir -p $out/lib/esp8266-rtos-sdk
    cp -r * $out/lib/esp8266-rtos-sdk
    cp ${libhal} $out/lib/esp8266-rtos-sdk/lib/libhal.a
  '';

  dontStrip = true;
}
