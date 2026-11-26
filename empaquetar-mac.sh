#!/bin/bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
bash compilar.sh
mkdir -p package-input/lib
cp EstacionSanJavier.jar package-input/
cp lib/*.jar package-input/lib/
directorio_temporal=$(mktemp -d "${TMPDIR:-/tmp}/eess-empaquetado.XXXXXX")
# Crear fuera del Escritorio evita metadatos de Finder durante la firma nativa.
jpackage --type app-image --name 'San Javier' --input package-input \
  --main-jar EstacionSanJavier.jar --main-class eess.Main --dest "$directorio_temporal" \
  --app-version 1.0 --description 'Gestión de estación de servicio San Javier' \
  --mac-package-identifier bo.sanjavier.eess \
  --add-modules java.desktop,java.sql,java.naming,java.management,java.security.jgss,java.scripting,jdk.crypto.ec,jdk.unsupported
if [ -e 'San Javier.app' ]; then
  mv 'San Javier.app' "$directorio_temporal/Anterior.app"
fi
mv "$directorio_temporal/San Javier.app" .
printf 'Aplicación creada: San Javier.app\n'
