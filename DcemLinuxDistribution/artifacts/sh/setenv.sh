#!/bin/bash
# -----------------------------------------------------------------------------
# Set java exe and conf file for all scripts
#
# -----------------------------------------------------------------------------

echo '++++++++++ YAJSW SET ENV ++++++++++'

#remember current dir
current=$(pwd)
# resolve links - $0 may be a softlink
PRGDIR=$(dirname $0)

cd "$PRGDIR"

# path to yajsw bin folder
PRGDIR=$(pwd)

cd ".."

# path to wrapper home
wrapper_home=$(pwd)
export wrapper_home

# path to dcem home
DCEM_HOME=$(pwd)/DCEM_HOME
export DCEM_HOME



# return to original folder
cd "$current"

wrapper_jar="$wrapper_home"/wrapper.jar
export wrapper_jar

wrapper_app_jar="$wrapper_home"/wrapperApp.jar
export wrapper_app_jar

wrapper_java_sys_options=-Djna_tmpdir="$wrapper_home"/tmp
export wrapper_java_sys_options

wrapper_java_options=-Xmx30m
export wrapper_java_options

java_exe="$wrapper_home"/jvm/bin/java
export java_exe

#conf_file="$wrapper_home"/conf/DcemApplication.conf
export conf_file

conf_default_file="$wrapper_home"/conf/wrapper.conf.default
export conf_default_file

echo "wrapper home : $wrapper_home"
echo "configuration: $conf_file"
echo "DCEM_HOME : $DCEM_HOME" 

# show java version
"$java_exe" -version
echo '---------- YAJSW SET ENV ----------'