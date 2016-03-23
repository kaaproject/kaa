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
    cp ${libhal} $out/lib/esp8266-rtos-sdk
  '';

  dontStrip = true;
}
