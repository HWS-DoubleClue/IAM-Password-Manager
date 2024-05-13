#!/bin/bash
# -----------------------------------------------------------------------------
# Start DCEM Setup
#
cd ..
./jvm/bin/java -DDCEM_HOME="$(pwd)/DCEM_HOME" -DINSTALLATION_PATH="$(pwd)" -DdebugLog=false -Dlog4jdbc=false -jar "./DcemSetup.jar"
