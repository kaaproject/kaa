{ stdenv, wine, requireFile, xvfb_run
}:
stdenv.mkDerivation rec {

  name = "cc3200-sdk-1.1.0";

  src = requireFile {
    name = "CC3200SDK-1.1.0-windows-installer.exe";
    sha256 = "1m5yjp2wjpgbjkq200b700gmz974519fw7rxby8cfxirkzknicw1";
    url = "http://www.ti.com/tool/cc3200sdk";
  };

  nativeBuildInputs = [ xvfb_run wine ];

  unpackPhase = ":";

  installPhase = ''
    mkdir -p $out/lib/cc3200-sdk
    WINEPREFIX=$PWD/.wine xvfb-run wine ${src} --mode unattended --prefix $out/lib/cc3200-sdk/
  '';
}
