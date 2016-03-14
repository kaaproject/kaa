{ stdenv, fetchFromGitHub
}:
let
  toolchain-name =
    if stdenv.system == "i686-linux" then "gcc-linaro-arm-linux-gnueabihf-raspbian" else
    if stdenv.system == "x86_64-linux" then "gcc-linaro-arm-linux-gnueabihf-raspbian-x64" else
    abort "only Linux is supported";

  executables = [
    "bin/arm-linux-gnueabihf-addr2line"
    "bin/arm-linux-gnueabihf-ar"
    "bin/arm-linux-gnueabihf-as"
    "bin/arm-linux-gnueabihf-c++filt"
    "bin/arm-linux-gnueabihf-cpp"
    "bin/arm-linux-gnueabihf-dwp"
    "bin/arm-linux-gnueabihf-elfedit"
    "bin/arm-linux-gnueabihf-g++"
    "bin/arm-linux-gnueabihf-gcc-4.8.3"
    "bin/arm-linux-gnueabihf-gcc-ar"
    "bin/arm-linux-gnueabihf-gcc-nm"
    "bin/arm-linux-gnueabihf-gcc-ranlib"
    "bin/arm-linux-gnueabihf-gcov"
    "bin/arm-linux-gnueabihf-gdb"
    "bin/arm-linux-gnueabihf-gfortran"
    "bin/arm-linux-gnueabihf-gprof"
    "bin/arm-linux-gnueabihf-ld.bfd"
    "bin/arm-linux-gnueabihf-ld.gold"
    "bin/arm-linux-gnueabihf-nm"
    "bin/arm-linux-gnueabihf-objcopy"
    "bin/arm-linux-gnueabihf-objdump"
    "bin/arm-linux-gnueabihf-pkg-config-real"
    "bin/arm-linux-gnueabihf-ranlib"
    "bin/arm-linux-gnueabihf-readelf"
    "bin/arm-linux-gnueabihf-size"
    "bin/arm-linux-gnueabihf-strings"
    "bin/arm-linux-gnueabihf-strip"
    "libexec/gcc/arm-linux-gnueabihf/4.8.3/cc1"
    "libexec/gcc/arm-linux-gnueabihf/4.8.3/cc1plus"
    "libexec/gcc/arm-linux-gnueabihf/4.8.3/collect2"
    "libexec/gcc/arm-linux-gnueabihf/4.8.3/f951"
    "libexec/gcc/arm-linux-gnueabihf/4.8.3/lto1"
    "libexec/gcc/arm-linux-gnueabihf/4.8.3/lto-wrapper"
    "arm-linux-gnueabihf/bin/ar"
    "arm-linux-gnueabihf/bin/as"
    "arm-linux-gnueabihf/bin/c++"
    "arm-linux-gnueabihf/bin/g++"
    "arm-linux-gnueabihf/bin/gcc"
    "arm-linux-gnueabihf/bin/gfortran"
    "arm-linux-gnueabihf/bin/ld"
    "arm-linux-gnueabihf/bin/ld.bfd"
    "arm-linux-gnueabihf/bin/ld.gold"
    "arm-linux-gnueabihf/bin/nm"
    "arm-linux-gnueabihf/bin/objcopy"
    "arm-linux-gnueabihf/bin/objdump"
    "arm-linux-gnueabihf/bin/ranlib"
    "arm-linux-gnueabihf/bin/strip"
  ];

in stdenv.mkDerivation {
  name = "raspberrypi-tools-20160311";

  src = fetchFromGitHub {
    owner = "raspberrypi";
    repo = "tools";
    rev = "3a413ca2b23fd275e8ddcc34f3f9fc3a4dbc723f";
    sha256 = "0qsgp34sb2nra0kyh8hbb9w91padqdhqjp7nsny96xh0xw24zwys";
  };

  installPhase = ''
    mkdir $out
    cp -r arm-bcm2708/${toolchain-name}/* $out
  '';

  dontStrip = true;

  preFixup = ''
    for executable in ${stdenv.lib.concatMapStringsSep " " (s: "$out/" + s) executables}; do
      patchelf --interpreter "${stdenv.glibc}/lib/${stdenv.cc.dynamicLinker}" \
        "$(readlink -f "$executable")"
    done
  '';
}
