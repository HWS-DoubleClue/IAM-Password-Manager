CD ..
set INSTALLATION_PATH=%CD%
REM "%INSTALLATION_PATH%\jvm\bin\java" -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8998,suspend=y -DDCEM_HOME="%INSTALLATION_PATH%\DCEM_HOME" -DINSTALLATION_PATH="%INSTALLATION_PATH%" -DdebugLog=false -Dlog4jdbc=false -jar "%INSTALLATION_PATH%\DcemApplication.jar"
EXIT /B %ERRORLEVEL%
