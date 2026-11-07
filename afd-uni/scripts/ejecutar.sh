#!/usr/bin/env bash
#
# Ejecuta la aplicación de escritorio desde el jar empaquetado.
# Si el jar no existe todavía, lo compila con Maven.
#
# Uso:  ./scripts/ejecutar.sh
#
set -euo pipefail

DIRECTORIO="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAR="$DIRECTORIO/target/afd-uni.jar"

if [[ ! -f "$JAR" ]]; then
    echo "No existe $JAR — compilando con Maven…"
    ( cd "$DIRECTORIO" && mvn -q clean package )
fi

# Se prefiere el Java de JAVA_HOME y, si no está definido, el del PATH.
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then
    JAVA_BIN="$JAVA_HOME/bin/java"
elif command -v java >/dev/null 2>&1; then
    JAVA_BIN="java"
else
    echo "ERROR: no se encontró Java. Instala un JRE/JDK 17 o define JAVA_HOME." >&2
    exit 1
fi

exec "$JAVA_BIN" -Dfile.encoding=UTF-8 -jar "$JAR" "$@"
