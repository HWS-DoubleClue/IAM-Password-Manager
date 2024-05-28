#!/bin/bash
# -----------------------------------------------------------------------------
# Start DCEM Application
#
cd ..
./jvm/bin/java -classpath "DcemApplication.jar:bin/*:Plugins/*" -DDCEM_HOME="$(pwd)/DCEM_HOME" -DINSTALLATION_PATH="$(pwd)" -DdebugLog=false -Dlog4jdbc=false com.doubleclue.dcem.app.DcemMain
