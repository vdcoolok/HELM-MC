@echo off
setlocal
cd /d "%~dp0"
call gradlew.bat helmBuild --console=plain --warning-mode all %*
exit /b %ERRORLEVEL%
