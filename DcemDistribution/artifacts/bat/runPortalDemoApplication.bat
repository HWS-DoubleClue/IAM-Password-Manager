CD ..
set dcem_installation_path=%CD%
"%dcem_installation_path%\jvm\bin\java" -DDCEM_HOME="%dcem_installation_path%/DCEM_HOME" -DINSTALLATION_PATH="%dcem_installation_path%" -DdebugLog=false -Dlog4jdbc=false -jar "%dcem_installation_path%\PortalDemo.jar"
EXIT /B %ERRORLEVEL%


