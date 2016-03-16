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
