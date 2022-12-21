#!/bin/bash
# -----------------------------------------------------------------------------
# run DCEM with remoteDebugging
#
# -----------------------------------------------------------------------------

# resolve links - $0 may be a softlink
exec ./ujvm/bin/java -DDCEM_HOME=/home/devuser/DcemInstallation-1.5.1/DCEM_HOME -Xdebug -agentlib:jdwp=transport=dt_socket,address=9999,server=y,suspend=n -jar DcemApplication.jar 
