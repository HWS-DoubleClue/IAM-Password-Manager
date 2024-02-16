CD ..
set dcem_installation_path=%CD%
"%dcem_installation_path%\jvm\bin\java" -classpath "DcemApplication.jar;bin\*;Plugins\*" -DDCEM_HOME="%dcem_installation_path%/DCEM_HOME" -DINSTALLATION_PATH="%dcem_installation_path%" -DdebugLog=false -Dlog4jdbc=false com.doubleclue.dcem.app.DcemMain
EXIT /B %ERRORLEVEL%