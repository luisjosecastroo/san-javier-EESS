#!/bin/bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
if ! bash ./compilar.sh; then
  printf '\nNo se pudo compilar. Revisa que tengas instalado un JDK 17 o posterior.\n'
  read -r -p 'Presiona Enter para cerrar.' respuesta
  exit 1
fi
java -jar EstacionSanJavier.jar "$@"
