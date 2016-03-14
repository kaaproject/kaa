{ stdenv, fetchFromGitHub
, autoconf, gperf, which, flex, bison, texinfo, wget
, libtool, automake, ncurses, gcc
, expat, file, unzip
}:
stdenv.mkDerivation {
  name = "gcc-xtensa-lx106-20150607";

  src = fetchFromGitHub {
    owner = "jcmvbkbc";
    repo = "crosstool-NG";
    rev = "f4a3946c3e120d1826bf458a8c1c4c50bc19d9e9";
    sha256 = "0xnh8idxxyjdb8lwrrmxm626wmzazkyp990vp29qv26fmnb3a1j0";
  };

  buildInputs = [
    stdenv
    autoconf
    gperf
    which
    flex
    bison
    texinfo
    wget

    libtool
    automake
    ncurses

    expat
    file
    unzip

    gcc
  ];

  preConfigure = ''
    ./bootstrap
  '';

  buildPhase = ''
    make && make install
    cp -r local-patches/ overlays/ $out/lib/ct-ng.1.20.0/
    cd $out
    ./bin/ct-ng xtensa-lx106-elf
    echo 'CT_HOST_PREFIX=${gcc}/bin/' >> .config
    ./bin/ct-ng build
  '';

  installPhase = ''
    for executable in $out/lib/ct-ng.1.20.0/builds/xtensa-lx106-elf/bin/*; do
      ln -s $executable $out/bin/$(basename $executable)
    done
  '';

  dontStrip = true;
}
