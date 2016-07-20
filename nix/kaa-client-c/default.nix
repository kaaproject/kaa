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
{ stdenv
, lib
, writeTextFile
, cmake

, astyle ? null
, doxygen ? null

, clang ? null
, openssl ? null

, cmocka ? null
, cppcheck ? null
, valgrind ? null
, python ? null
, gcc-arm-embedded ? null
, jre ? null

, gcc-xtensa-lx106 ? null
, esp8266-rtos-sdk ? null
, cc3200-sdk ? null

, raspberrypi-tools ? null
, raspberrypi-openssl ? null

, clangSupport ? true
, posixSupport ? true
, cc3200Support ? true
, esp8266Support ? true
, raspberrypiSupport ? true
, testSupport ? true
, withTooling ? true
, withWerror ? false
}:

assert clangSupport -> clang != null && openssl != null;
assert posixSupport -> openssl != null;
assert esp8266Support -> gcc-xtensa-lx106 != null && esp8266-rtos-sdk != null && jre != null;
assert cc3200Support -> cc3200-sdk != null && gcc-arm-embedded != null && jre != null;
assert raspberrypiSupport -> raspberrypi-tools != null && raspberrypi-openssl != null;
assert testSupport -> posixSupport != null && cmocka != null && cppcheck != null &&
                      valgrind != null && python != null;

let
  kaa-generic-makefile =
    let
      target = enable: name: cmake_options:
        lib.optionalString enable
          ''
            .PHONY: __propagate_${name}
            __propagate: __propagate_${name}
            __propagate_${name}: build-${name}/Makefile
            > make -C build-${name} $(ARGS)

            build-${name}/Makefile: Makefile
            > cmake -U * -Bbuild-${name} -H. ${cmake_options} "-DCMAKE_EXPORT_COMPILE_COMMANDS=ON" \
                ${lib.optionalString withWerror "-DCMAKE_C_FLAGS=-Werror"}
          '';
    in writeTextFile {
      name = "kaa-generic-makefile";
      destination = "/Makefile";
      text = ''
        .RECIPEPREFIX := >
        .PHONY: __propagate

        .DEFAULT:
        > @if [ -z "$(SECONDRUN)" ]; then make ARGS="$(MAKECMDGOALS)"; fi
        > $(eval SECONDRUN:=true)

        __propagate:;
      ''
      + target posixSupport "posix"
              "${lib.optionalString testSupport ''-DCMAKE_BUILD_TYPE=Debug -DKAA_UNITTESTS_COMPILE=on -DKAA_COLLECT_COVERAGE=1''}"
      + target posixSupport "nologs"
              "${lib.optionalString testSupport ''-DKAA_UNITTESTS_COMPILE=on''} -DKAA_MAX_LOG_LEVEL=0"
      + target clangSupport "clang"
              "${lib.optionalString testSupport ''-DKAA_UNITTESTS_COMPILE=on''} -DCMAKE_C_COMPILER=clang -DCMAKE_CXX_COMPILER=clang++"
      + target cc3200Support "cc3200"
              "-DKAA_PLATFORM=cc32xx -DCMAKE_TOOLCHAIN_FILE=toolchains/cc32xx.cmake -DCC32XX_SDK='${cc3200-sdk}/lib/cc3200-sdk/cc3200-sdk' -DCC32XX_TOOLCHAIN_PATH='${gcc-arm-embedded}'"
      + target esp8266Support "esp8266"
              "-DKAA_PLATFORM=esp8266 -DCMAKE_TOOLCHAIN_FILE=toolchains/esp8266.cmake -DESP8266_TOOLCHAIN_PATH='${gcc-xtensa-lx106}' -DESP8266_SDK_PATH='${esp8266-rtos-sdk}/lib/esp8266-rtos-sdk'"
      + target raspberrypiSupport "rpi"
              "-DCMAKE_PREFIX_PATH=${raspberrypi-openssl} -DCMAKE_TOOLCHAIN_FILE=toolchains/rpi.cmake";
    };
in stdenv.mkDerivation {
  name = "kaa-client-c";

  buildInputs = [
    kaa-generic-makefile
    cmake
  ] ++ lib.optional withTooling [
    astyle
    doxygen
  ] ++ lib.optional clangSupport [
    clang
    openssl
  ] ++ lib.optional posixSupport [
    openssl
  ] ++ lib.optional testSupport [
    cmocka
    cppcheck
    valgrind
    python
  ] ++ lib.optional esp8266Support [
    gcc-xtensa-lx106
    esp8266-rtos-sdk
    jre
  ] ++ lib.optional cc3200Support [
    cc3200-sdk
    gcc-arm-embedded
    jre
  ] ++ lib.optional raspberrypiSupport [
    raspberrypi-tools
    raspberrypi-openssl
  ];

  shellHook = ''
    export CTEST_OUTPUT_ON_FAILURE=1

    cp ${kaa-generic-makefile}/Makefile .
    chmod 644 Makefile
  '';
}
