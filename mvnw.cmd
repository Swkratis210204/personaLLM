@REM Maven Wrapper for Windows
@REM Looks for Maven in PATH or common install locations.
@REM Usage: mvnw.cmd <goal>  (e.g. mvnw.cmd package)

@echo off

where mvn >nul 2>&1
if %ERRORLEVEL% == 0 (
    mvn %*
    exit /b %ERRORLEVEL%
)

echo.
echo ERROR: Maven not found in PATH.
echo Please install Maven from https://maven.apache.org/download.cgi
echo and add it to your PATH, then try again.
echo.
exit /b 1
