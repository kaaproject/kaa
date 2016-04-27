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
{ stdenv, fetchurl, cmake, boost155, python
}:

stdenv.mkDerivation {
  name = "avro-cpp-1.7.5";

  buildInputs = [
    cmake
    boost155
    python
  ];

  src = fetchurl {
    url = "http://archive.apache.org/dist/avro/avro-1.7.5/cpp/avro-cpp-1.7.5.tar.gz";
    sha256 = "064ssbbgrc3hyalzj8rn119bsrnyk1vlpkhl8gghv96jgqbpdyb3";
  };

  enableParallelBuilding = true;
}
