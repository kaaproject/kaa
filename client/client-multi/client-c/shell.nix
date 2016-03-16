{ system ? builtins.currentSystem
, posixSupport ? true
, clangSupport ? true
, cc3200Support ? true
, esp8266Support ? true
, raspberrypiSupport ? true
, testSupport ? true
, withCUnit ? true
}:

assert testSupport -> posixSupport;

let
  pkgs = import <nixpkgs> { inherit system; };

  callPackage = pkgs.lib.callPackageWith (pkgs // self);

  self = rec {
    gcc-xtensa-lx106 = callPackage ./nix/gcc-xtensa-lx106 { };

    esp-open-sdk = callPackage ./nix/esp-open-sdk { };

    esp8266-rtos-sdk = callPackage ./nix/esp8266-rtos-sdk { };

    cc3200-sdk = callPackage ./nix/cc3200-sdk { };

    raspberrypi-tools = callPackage ./nix/raspberrypi-tools { };

    raspberrypi-openssl = callPackage ./nix/raspberrypi-openssl { };

    kaa-generic-makefile = pkgs.writeTextFile {
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

  target = enable: name: cmake_options:
    pkgs.lib.optionalString enable
      ''
        .PHONY: __propagate_${name}
        __propagate: __propagate_${name}
        __propagate_${name}: build-${name}/Makefile
        > make -C build-${name} $(ARGS)

        build-${name}/Makefile:
        > mkdir -p build-${name}
        > cmake -Bbuild-${name} -H. ${cmake_options}
      '';

in with self; with pkgs; {
  kaaEnv = stdenv.mkDerivation {
    name = "kaa-env";
    buildInputs = [
      kaa-generic-makefile
      cmake
    ] ++ lib.optional clangSupport [
      clang
      openssl
    ] ++ lib.optional posixSupport [
      openssl
    ] ++ lib.optional withCUnit [
      cunit
    ] ++ lib.optional testSupport [
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

    shellHook =
      lib.optionalString cc3200Support ''
        export CC32XX_SDK=${cc3200-sdk}/lib/cc3200-sdk/cc3200-sdk
      '' +
      lib.optionalString esp8266Support ''
        export ESP8266_TOOLCHAIN_PATH="${gcc-xtensa-lx106}/lib/ct-ng.1.20.0/builds/xtensa-lx106-elf"
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
