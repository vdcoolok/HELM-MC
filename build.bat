@echo off
setlocal
cd /d "%~dp0"

call gradlew.bat helmBuild --console=plain --warning-mode all %*
if errorlevel 1 exit /b %ERRORLEVEL%

set "JAR="
for %%f in (build\libs\HELM*.jar) do (
    if not defined JAR set "JAR=%%f"
)

if not defined JAR (
    echo Build produced no jar in build\libs. 1>&2
    exit /b 1
)

copy /y "%JAR%" "%~dp0%%~nxf" >nul
echo Jar written to %~dp0%%~nxf
