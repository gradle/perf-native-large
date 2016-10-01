#!/bin/bash
# This script will execute a Gradle build several times with JFR profiling
#
# Usage:
#   $ ./jfr_prof.sh [-w warmups] [-r runs] [build parameters]
# * warmups: the number of warmups. Defaults to 3. Warmups are excluded from profiling.
# * runs: the number of runs. Defaults to 10. Runs are executed with Honest Profiler activated. See below.
# * build parameters: parameters (tasks) to be passed to the build. Defaults to "help"
#
# The script requires the following properties to be configured for YOUR environment
#
# * JAVA_HOME : path to the JDK
# * GRADLE_BIN : path to the gradle binary which will be used to execute the builds

WORKDIR="$PWD"
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [ -z "$JAVA_HOME" ]; then
    >&2 echo "JAVA_HOME is not set."
    exit 1
fi
if [ -z "$GRADLE_BIN" ]; then
    >&2 echo "GRADLE_BIN is not set. It should point to the gradle command to use."
    exit 1
fi

warmups=3
runs=10
allocations=0

while getopts "aw:r:" opt; do
    case "${opt}" in
        a)
        allocations=1
        ;;
        w)
        warmups="${OPTARG}"
        ;;
        r)
        runs="${OPTARG}"
        ;;
    esac
done
shift $((OPTIND-1))

if [ "$#" -eq 0 ]; then
  buildparams=( "help" )
else
  buildparams=( "$@" )
fi

extra_opts=()
jfr_opts="stackdepth=1024,disk=true"
if [ $allocations -eq 1 ]; then
    # TLAB allocations must be disabled to profile all allocations, this adds a lot of overhead
    extra_opts=("${extra_opts[@]}" -XX:-UseTLAB -XX:MaxJavaStackTraceDepth=20)
    jfr_opts="stackdepth=1024,disk=true,globalbuffersize=500M,maxchunksize=120M,threadbuffersize=200k"
fi
if [ -f $WORKDIR/gradle.properties ]; then
    mem_args=`egrep "^org.gradle.jvmargs=" $WORKDIR/gradle.properties | awk -F= '{ print $2 }'`
fi
if [ -z "$mem_args" ]; then
    mem_args="-Xmx4g -Xverify:none"
fi
export GRADLE_OPTS="-Dorg.gradle.jvmargs='${mem_args} ${extra_opts[@]} -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=${jfr_opts}'"

# END OF CONFIGURATION

echo "Killing all daemons before measurements"
ps ax | grep GradleDaemon | awk '{print $1}' | xargs kill -9

warmups_done=0
for n in $(seq $warmups); do 
   echo "Warmup... $n/$warmups"
   $GRADLE_BIN --daemon -u "${buildparams[@]}"
   warmups_done=1
done

# use 'help' to start daemon when warmup count is zero
if [ $warmups_done -eq 0 ]; then
   $GRADLE_BIN --daemon -u help
fi

echo "Starting profiler"
[ -f gradle.pid ] && DAEMON_PID=`cat gradle.pid`
[ -n "$DAEMON_PID" ] || DAEMON_PID=`jps | grep GradleDaemon | awk '{ print $1 }'`
JFR_FILENAME="$WORKDIR/GradleDaemon_${DAEMON_PID}_$(date +%F-%T).jfr"
jcmd $DAEMON_PID JFR.start name=GradleDaemon_$DAEMON_PID settings=$DIR/profiling.jfc filename=$JFR_FILENAME dumponexit=true

echo "Measuring..."
for n in $(seq $runs); do 
  echo "Iteration $n/$runs"
  $GRADLE_BIN --daemon -u "${buildparams[@]}"
done

jcmd $DAEMON_PID JFR.stop name=GradleDaemon_$DAEMON_PID

echo "Output in $JFR_FILENAME ."
jmc -open "$JFR_FILENAME" &