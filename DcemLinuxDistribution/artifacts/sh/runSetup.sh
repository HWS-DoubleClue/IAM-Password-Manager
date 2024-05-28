#!/bin/bash
# -----------------------------------------------------------------------------
# Start DCEM Setup
#
cd ..
./jvm/bin/java -classpath "DcemSetup.jar:bin/*:Plugins/*" -DDCEM_HOME="$(pwd)/DCEM_HOME" -DINSTALLATION_PATH="$(pwd)" -DdebugLog=false -Dlog4jdbc=false com.doubleclue.dcem.setup.MainSetup
