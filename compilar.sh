#!/bin/bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
mkdir -p out
find src -name '*.java' -print > out/fuentes.txt
javac --release 17 -encoding UTF-8 -Xlint:all -cp 'lib/*' -d out @out/fuentes.txt
printf 'Main-Class: eess.Main\nClass-Path: lib/postgresql-42.7.15.jar lib/h2-2.5.252.jar\n\n' > out/MANIFEST.MF
jar --create --file EstacionSanJavier.jar --manifest out/MANIFEST.MF -C out eess -C . sql
printf 'Compilación correcta: EstacionSanJavier.jar\n'
