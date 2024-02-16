#!/bin/bash
# -----------------------------------------------------------------------------
# Start DCEM Application
#
cd ..
./jvm/bin/java  -DDCEM_HOME="$(pwd)/DCEM_HOME" -DINSTALLATION_PATH="$(pwd)" -DdebugLog=false -Dlog4jdbc=false -jar "./DcemApplication.jar"
