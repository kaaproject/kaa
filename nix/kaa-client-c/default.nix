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

, cmocka ? null
, cppcheck ? null
, valgrind ? null
, python ? null
, gcc-arm-embedded ? null

, gcc-xtensa-lx106 ? null
, esp8266-rtos-sdk ? null
, cc3200-sdk ? null

, raspberrypi-tools ? null

, clangSupport ? true
, posixSupport ? true
, cc3200Support ? true
, esp8266Support ? true
, raspberrypiSupport ? true
, testSupport ? true
, withTooling ? true
, withWerror ? false
, withValgrind ? true
}:

assert clangSupport -> clang != null;
assert esp8266Support -> gcc-xtensa-lx106 != null && esp8266-rtos-sdk != null;
assert cc3200Support -> cc3200-sdk != null && gcc-arm-embedded != null;
assert raspberrypiSupport -> raspberrypi-tools != null;
assert testSupport -> cmocka != null && cppcheck != null && python != null;

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
              "${if testSupport then ''-DCMAKE_BUILD_TYPE=Debug -DKAA_COLLECT_COVERAGE=1'' else ''-DBUILD_TESTING=OFF''}"
      + target posixSupport "nologs"
              "${lib.optionalString (!testSupport) ''-DBUILD_TESTING=OFF''} -DKAA_MAX_LOG_LEVEL=0"
      + target clangSupport "clang"
              "${lib.optionalString (!testSupport) ''-DBUILD_TESTING=OFF''} -DCMAKE_C_COMPILER=clang -DCMAKE_CXX_COMPILER=clang++"
      + target cc3200Support "cc3200"
              "-DKAA_PLATFORM=cc32xx -DCMAKE_TOOLCHAIN_FILE=toolchains/cc32xx.cmake -DBUILD_TESTING=OFF -DCC32XX_SDK='${cc3200-sdk}/lib/cc3200-sdk/cc3200-sdk' -DCC32XX_TOOLCHAIN_PATH='${gcc-arm-embedded}'"
      + target esp8266Support "esp8266"
              "-DKAA_PLATFORM=esp8266 -DCMAKE_TOOLCHAIN_FILE=toolchains/esp8266.cmake -DBUILD_TESTING=OFF -DESP8266_TOOLCHAIN_PATH='${gcc-xtensa-lx106}' -DESP8266_SDK_PATH='${esp8266-rtos-sdk}/lib/esp8266-rtos-sdk'"
      + target raspberrypiSupport "rpi"
              "-DCMAKE_TOOLCHAIN_FILE=toolchains/rpi.cmake -DBUILD_TESTING=OFF";
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
  ] ++ lib.optional testSupport [
    cmocka
    cppcheck
    python
  ] ++ lib.optional withValgrind [
    valgrind
  ] ++ lib.optional esp8266Support [
    gcc-xtensa-lx106
    esp8266-rtos-sdk
  ] ++ lib.optional cc3200Support [
    cc3200-sdk
    gcc-arm-embedded
  ] ++ lib.optional raspberrypiSupport [
    raspberrypi-tools
  ];

  shellHook = ''
    export CTEST_OUTPUT_ON_FAILURE=1

    cp ${kaa-generic-makefile}/Makefile .
    chmod 644 Makefile
  '';
}
