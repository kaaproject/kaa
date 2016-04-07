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
, posixSupport ? true
, clangSupport ? true
, cc3200Support ? true
, esp8266Support ? true
, raspberrypiSupport ? true
, testSupport ? true
, withWerror ? false
}:

assert testSupport -> posixSupport;

let
  pkgs = import <nixpkgs> { inherit system; };

  callPackage = pkgs.lib.callPackageWith (pkgs // self);

  self = rec {
    gcc-xtensa-lx106 = callPackage ./nix/gcc-xtensa-lx106 { };

    esp8266-rtos-sdk = callPackage ./nix/esp8266-rtos-sdk { };

    cc3200-sdk = callPackage ./nix/cc3200-sdk { };

    raspberrypi-tools = callPackage ./nix/raspberrypi-tools { };

    raspberrypi-openssl = callPackage ./nix/raspberrypi-openssl { };

    # Currently, it causes compilation failure, so we use 4.7 for now.
    # gcc-arm-embedded = pkgs.callPackage_i686 ./nix/gcc-arm-embedded {
    #   dirName = "5.0";
    #   subdirName = "5-2015-q4-major";
    #   version = "5.2-2015q4-20151219";
    #   releaseType = "major";
    #   sha256 = "12mbwl9iwbw7h6gwwkvyvfmrsz7vgjz27jh2cz9z006ihzigi50y";
    # };
    gcc-arm-embedded = pkgs.gcc-arm-embedded-4_7;

    astyle = pkgs.astyle.overrideDerivation (self: {
      sourceRoot = "astyle";
      preBuild = ''
        cd build/${if self.stdenv.cc.isClang then "clang" else "gcc"}
      '';
      patches = [ ./nix/astyle/max_indent.patch ];
    });

    kaa-generic-makefile =
      let
        target = enable: name: cmake_options:
          pkgs.lib.optionalString enable
            ''
              .PHONY: __propagate_${name}
              __propagate: __propagate_${name}
              __propagate_${name}: build-${name}/Makefile
              > make -C build-${name} $(ARGS)

              build-${name}/Makefile: Makefile
              > cmake -U * -Bbuild-${name} -H. ${cmake_options} ${pkgs.lib.optionalString withWerror "-DCMAKE_C_FLAGS=-Werror"}
            '';
    in pkgs.writeTextFile {
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
              ""
      + target clangSupport "clang"
              "-DCMAKE_C_COMPILER=clang -DCMAKE_CXX_COMPILER=clang++"
      + target cc3200Support "cc3200"
              "-DKAA_PLATFORM=cc32xx -DCMAKE_TOOLCHAIN_FILE=toolchains/cc32xx.cmake"
      + target esp8266Support "esp8266"
              "-DKAA_PLATFORM=esp8266 -DCMAKE_TOOLCHAIN_FILE=toolchains/esp8266.cmake"
      + target raspberrypiSupport "rpi"
               "-DCMAKE_PREFIX_PATH=${raspberrypi-openssl} -DCMAKE_TOOLCHAIN_FILE=toolchains/rpi.cmake";
    };
  };

in with self; with pkgs; {
  kaaEnv = stdenv.mkDerivation {
    name = "kaa-env";
    buildInputs = [
      kaa-generic-makefile
      cmake
      self.astyle
      maven
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
      self.gcc-arm-embedded
      jre
    ] ++ lib.optional raspberrypiSupport [
      raspberrypi-tools
      raspberrypi-openssl
    ];

    shellHook =
      lib.optionalString cc3200Support ''
        export CC32XX_SDK=${cc3200-sdk}/lib/cc3200-sdk/cc3200-sdk
      '' +
      lib.optionalString esp8266Support ''
        export ESP8266_TOOLCHAIN_PATH="${gcc-xtensa-lx106}"
        export ESP8266_SDK_BASE=${esp8266-rtos-sdk}/lib/esp8266-rtos-sdk
      '' +
      ''
        cp ${kaa-generic-makefile}/Makefile .
        chmod 644 Makefile

        cat <<EOF > ./.zshrc
        source \$HOME/.zshrc
        PROMPT="%B%F{red}[kaa]%f%b \$PROMPT"
        EOF

        export ZDOTDIR=.;

        # zsh -i; exit
      '';
  };
}
