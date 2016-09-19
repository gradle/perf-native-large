#!/bin/bash
# This script will execute a Gradle build several times with profiling to generate a flame graph that
# can be used to diagnose performance issues.
# Usage:
#   $ ./prof.sh [-f basename] [-i interval] [-w warmups] [-r runs] [build parameters]
# * basename: name of the generated flame graph file. Defaults to "flames", which will generate "flames.svg"
# * interval: the sampling interval. Defaults to 7ms, the time between 2 samples. Should preferably be a prime number.
# * warmups: the number of warmups. Defaults to 3. Warmups are excluded from profiling.
# * runs: the number of runs. Defaults to 10. Runs are executed with Honest Profiler activated. See below.
# * build parameters: parameters (tasks) to be passed to the build. Defaults to "help"
#
# The script requires the following properties to be configured for YOUR environment
#
# * JAVA_HOME : path to the JDK
# * HP_HOME_DIR: path to an installation of honest profiler. This must be compiled from sources (https://github.com/RichardWarburton/honest-profiler)
# * FG_HOME_DIR: path to an installation of FlameGraph. This is a clone of this repository : https://github.com/brendangregg/FlameGraph
# * GRADLE_BIN : path to the gradle binary which will be used to execute the builds

WORKDIR="$PWD"
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
export STACKTRACE_SANITIZER="$DIR/filter_flames.groovy"
export GROOVY_OPTS="-Xms2g -Xmx2g"

if [ -z "$JAVA_HOME" ]; then
    >&2 echo "JAVA_HOME is not set."
    exit 1
fi
if [ -z "$HP_HOME_DIR" ]; then
    >&2 echo "HP_HOME_DIR is not set. It's where honest-profiler is installed."
    exit 1
fi
if [ -z "$FG_HOME_DIR" ]; then
    >&2 echo "FG_HOME_DIR is not set. It's where flamegraph.pl is installed."
    exit 1
fi
if [ -z "$GRADLE_BIN" ]; then
    >&2 echo "GRADLE_BIN is not set. It should point to the gradle command to use."
    exit 1
fi

filename=flames
interval=7
warmups=3
runs=10

while getopts "f:i:w:r:" opt; do
    case "${opt}" in
        f)
        filename="${OPTARG}"
        ;;
        i)
        interval="${OPTARG}"
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

if [ -f $WORKDIR/gradle.properties ]; then
    mem_args=`egrep "^org.gradle.jvmargs=" $WORKDIR/gradle.properties | awk -F= '{ print $2 }'`
fi
if [ -z "$mem_args" ]; then
    mem_args="-Xmx4g -Xverify:none"
fi
export GRADLE_OPTS="-Dorg.gradle.jvmargs='${mem_args} -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints -agentpath:$HP_HOME_DIR/liblagent.so=interval=$interval,logPath=$WORKDIR/hp.log,port=18080,host=localhost,start=0'"

# END OF CONFIGURATION

rm -f $WORKDIR/flames.txt
echo "Killing all daemons before measurements"
ps ax | grep GradleDaemon | awk '{print $1}' | xargs kill -9

echo "Profile to $filename at interval $interval"

rm -f $WORKDIR/hp.log
for n in $(seq $warmups); do 
   echo "Warmup... $n/$warmups"
   $GRADLE_BIN --daemon -u "${buildparams[@]}"
done
echo status | nc localhost 18080
echo "Starting profiler"
echo start | nc localhost 18080
echo status | nc localhost 18080
echo "Measuring..."
for n in $(seq $runs); do 
  echo "Iteration $n/$runs"
  $GRADLE_BIN --daemon -u "${buildparams[@]}"
done
echo stop | nc localhost 18080
echo status | nc localhost 18080

cp $WORKDIR/hp.log $WORKDIR/hp-ref.log

$JAVA_HOME/bin/java -cp $JAVA_HOME/lib/tools.jar:$HP_HOME_DIR/honest-profiler.jar com.insightfullogic.honest_profiler.ports.console.FlameGraphDumperApplication $WORKDIR/hp.log $WORKDIR/flames.txt
$STACKTRACE_SANITIZER $WORKDIR/flames.txt $WORKDIR/flames-sanitized.txt
$FG_HOME_DIR/flamegraph.pl --title "$($GRADLE_BIN --version|egrep 'Gradle|Revision' |xargs echo) Args: ${buildparams[@]} Date: $(date)" $WORKDIR/flames-sanitized.txt > $WORKDIR/$filename.svg

echo "Output in $WORKDIR/$filename.svg"