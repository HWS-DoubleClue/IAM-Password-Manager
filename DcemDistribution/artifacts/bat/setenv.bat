@echo off

set REL_PATH=%~dp0%..\

rem // Save current directory and change to target directory
pushd %REL_PATH%

rem // Save value of CD variable (current directory)
set dcem_installation_path=%CD%
rem // Restore original directory
popd

rem default java home
set wrapper_home=%~dp0%
echo wrapper_home-- %wrapper_home%
echo 

rem default java exe for running the wrapper
rem note this is not the java exe for running the application. the exe for running the application is defined in the wrapper configuration file
set java_exe="%dcem_installation_path%\jvm\bin\java"

rem location of the wrapper jar file. necessary lib files will be loaded by this jar. they must be at <wrapper_home>/lib/...
set wrapper_jar="%wrapper_home%../wrapper.jar"
set wrapper_app_jar="%wrapper_home%../wrapperApp.jar"
set wrapper_app9_jar="%wrapper_home%../wrapperApp9.jar"

rem wrapper bat file for running the wrapper
set wrapper_bat="%wrapper_home%wrapper.bat"
set wrapperw_bat="%wrapper_home%wrapperW.bat"

rem configuration file used by all bat files
set conf_file="%wrapper_home%..\conf\wrapper.conf"

rem Dcem Home Directory
set DCEM_HOME=%dcem_installation_path%\DCEM_HOME
echo "dcem_home: %DCEM_HOME%"

rem setting java options for wrapper process. depending on the scripts used, the wrapper may require more memory.
set wrapper_java_options=-Xmx2048m -Djna_tmpdir="%DCEM_HOME%\jna" -Djava.net.preferIPv4Stack=true -DDCEM_HOME="%DCEM_HOME%"

