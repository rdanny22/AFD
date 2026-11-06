#!/usr/bin/env bash
#
# Genera una aplicación nativa autocontenida con jpackage (JDK 14+).
#
# El resultado incluye una máquina virtual de Java propia: el equipo donde se
# ejecute NO necesita tener Java instalado.
#
# Uso:
#   ./scripts/empaquetar-nativo.sh              # app-image (carpeta con ejecutable)
#   ./scripts/empaquetar-nativo.sh msi          # instalador MSI (Windows, requiere WiX)
#   ./scripts/empaquetar-nativo.sh exe          # instalador EXE (Windows, requiere WiX)
#   ./scripts/empaquetar-nativo.sh deb          # paquete .deb (Linux)
#   ./scripts/empaquetar-nativo.sh rpm          # paquete .rpm (Linux)
#   ./scripts/empaquetar-nativo.sh dmg          # imagen .dmg (macOS)
#
# Requisitos:
#   * JDK completo 17 o superior (jpackage no viene en un JRE).
#   * El jar empaquetado (se compila aquí si falta).
#   * Cada instalador se genera en su propio sistema operativo.
#
# Icono opcional: exportar ICONO=/ruta/al/icono.png (o .ico en Windows, .icns en macOS)
#
set -euo pipefail

DIRECTORIO="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAR="$DIRECTORIO/target/afd-uni.jar"
ENTRADA="$DIRECTORIO/target/jpackage-input"
SALIDA="$DIRECTORIO/target/nativo"
NOMBRE="AFD-UNI"
VERSION="1.0.0"
TIPO="${1:-app-image}"

if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/jpackage" ]]; then
    JPACKAGE="$JAVA_HOME/bin/jpackage"
elif command -v jpackage >/dev/null 2>&1; then
    JPACKAGE="jpackage"
else
    echo "ERROR: jpackage no está disponible." >&2
    echo "Instala un JDK completo 17+ (no basta un JRE) o define JAVA_HOME." >&2
    exit 1
fi

if [[ ! -f "$JAR" ]]; then
    echo "No existe $JAR — compilando con Maven…"
    ( cd "$DIRECTORIO" && mvn -q clean package )
fi

# jpackage solo debe recibir el jar sombreado, no el original-afd-uni.jar que deja shade.
rm -rf "$ENTRADA"
mkdir -p "$ENTRADA"
cp "$JAR" "$ENTRADA/"
rm -rf "$SALIDA"
mkdir -p "$SALIDA"

ARGUMENTOS=(
    --type "$TIPO"
    --name "$NOMBRE"
    --app-version "$VERSION"
    --vendor "Universidad Nacional de Ingeniería"
    --description "Validador de correo institucional UNI mediante un AFD"
    --input "$ENTRADA"
    --main-jar "afd-uni.jar"
    --main-class "ni.edu.uni.afd.Main"
    --java-options "-Dfile.encoding=UTF-8"
    --dest "$SALIDA"
)

if [[ -n "${ICONO:-}" ]]; then
    ARGUMENTOS+=( --icon "$ICONO" )
fi

echo "Generando el paquete «$TIPO» con jpackage…"
"$JPACKAGE" "${ARGUMENTOS[@]}"

echo
echo "Listo. Contenido de $SALIDA:"
ls -1 "$SALIDA"
echo
echo "Ejecutable (app-image): $SALIDA/$NOMBRE/bin/$NOMBRE"
