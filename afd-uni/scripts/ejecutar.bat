@echo off
rem ---------------------------------------------------------------------------
rem  Ejecuta la aplicacion de escritorio en Windows sin ventana de consola.
rem  Si el jar no existe todavia, lo compila con Maven.
rem
rem  Uso:  scripts\ejecutar.bat
rem ---------------------------------------------------------------------------
setlocal
set "DIRECTORIO=%~dp0.."
set "JAR=%DIRECTORIO%\target\afd-uni.jar"

if not exist "%JAR%" (
    echo No existe %JAR% - compilando con Maven...
    pushd "%DIRECTORIO%"
    call mvn -q clean package
    popd
)

start "" javaw -Dfile.encoding=UTF-8 -jar "%JAR%"
endlocal
