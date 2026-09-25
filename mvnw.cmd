@ECHO OFF
setlocal

set MAVEN_VERSION=3.9.9
set WRAPPER_DIR=%~dp0.mvn\wrapper
set DIST_DIR=%WRAPPER_DIR%\dists\apache-maven-%MAVEN_VERSION%
set MAVEN_HOME=%DIST_DIR%\apache-maven-%MAVEN_VERSION%
set MVN_CMD=%MAVEN_HOME%\bin\mvn.cmd

if not exist "%MVN_CMD%" (
    echo Apache Maven %MAVEN_VERSION% not found locally, downloading...
    if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
    powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip' -OutFile '%DIST_DIR%\maven.zip'"
    if errorlevel 1 (
        echo Failed to download Apache Maven.
        exit /b 1
    )
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%DIST_DIR%\maven.zip' -DestinationPath '%DIST_DIR%' -Force"
    del "%DIST_DIR%\maven.zip"
)

call "%MVN_CMD%" %*
exit /b %ERRORLEVEL%
