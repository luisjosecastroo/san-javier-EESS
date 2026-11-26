#!/bin/bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
bash compilar.sh
mkdir -p out-test
find test -name '*.java' -print > out-test/fuentes.txt
javac --release 17 -encoding UTF-8 -Xlint:all -cp 'out:lib/*' -d out-test @out-test/fuentes.txt
java -Djava.awt.headless=true -cp 'out-test:out:lib/*:.' eess.PruebasEstacion
