{ stdenv, wine, requireFile, xvfb_run
}:
stdenv.mkDerivation rec {

  name = "cc3200-sdk-1.2.0";

  src = requireFile {
    name = "CC3200SDK-1.2.0-windows-installer.exe";
    sha256 = "1wdm52n7mx5w57l48gdl8387nsn2vgq3pwxy9z5zc7v9k16zcldh";
    url = "http://www.ti.com/tool/cc3200sdk";
  };

  nativeBuildInputs = [ xvfb_run wine ];

  unpackPhase = ":";

  installPhase = ''
    mkdir -p $out/lib/cc3200-sdk
    WINEPREFIX=$PWD/.wine xvfb-run wine ${src} --mode unattended --prefix $out/lib/cc3200-sdk/
  '';
}
