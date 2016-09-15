#!/bin/bash
# This script will execute a Gradle build several times with profiling to generate a flame graph that
# can be used to diagnose performance issues.
# Usage:
#   $ ./prof.sh [basename] [interval] [warmups] [runs] [tasks]
# * basename: name of the generated flame graph file. Defaults to "flames", which will generate "flames.svg"
# * interval: the sampling interval. Defaults to 7ms, the time between 2 samples. Should preferably be a prime number.
# * warmups: the number of warmups. Defaults to 3. Warmups are excluded from profiling.
# * runs: the number of runs. Defaults to 10. Runs are executed with Honest Profiler activated. See below.
# * tasks: the tasks to be executed. Defaults to "help"
#
# The script requires the following properties to be configured for YOUR environment
#
# * JAVA_HOME : path to the JDK
# * HP_HOME_DIR: path to an installation of honest profiler. This must be compiled from sources (https://github.com/RichardWarburton/honest-profiler)
# * FG_HOME_DIR: path to an installation of FlameGraph. This is a clone of this repository : https://github.com/brendangregg/FlameGraph
# * GROOVY_SANITIZER: path to the Groovy script which sanitizes stack frames 
# * GROOVY_OPTS: options passed to the Groovy script. Can be tweaked if not enough memory is used. 
# * gl : path to the Gradle installation which will be used to execute the builds

export JAVA_HOME=/opt/jdk1.8.0
export HP_HOME_DIR=/home/cchampeau/TOOLS/honest-profiler
export FG_HOME_DIR=/home/cchampeau/DEV/PROJECTS/GITHUB/FlameGraph
export GROOVY_SANITIZER=/home/cchampeau/TOOLS/filter-flames.groovy
export GROOVY_OPTS="-Xms2g -Xmx2g"

# EDIT THIS LINE TO TELL WHICH VERSION OF GRADLE IS GOING TO BE USED
gl='/home/cchampeau/DEV/gradle-source-build/bin/gradle'
#gl='/home/cchampeau/.sdkman/gradle/1.1/bin/gradle'
#gl='./gradlew'

filename=${1-'flames'}
interval=${2-'7'}
warmups=${3-'3'}
runs=${4-'10'}
tasks=${5-'help'}

export JAVA_OPTS="-Dorg.gradle.jvmargs='-Xmx8g -Xms8g -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints -agentpath:/home/cchampeau/TOOLS/honest-profiler/liblagent.so=interval=$interval,logPath=/tmp/hp.log,port=18080,host=localhost,start=0'"

# END OF CONFIGURATION


rm -f /tmp/flames.txt
echo "Killing all daemons before measurements"
ps ax | grep GradleDaemon | awk '{print $1}' | xargs kill -9

echo "Profile to $filename at interval $interval"


rm -f /tmp/hp.log
for n in $(seq $warmups); do 
   echo "Warmup... $n/$warmups"
   $gl --daemon -u $tasks
done
echo status | nc localhost 18080
echo "Starting profiler"
echo start | nc localhost 18080
echo status | nc localhost 18080
echo "Measuring..."
for n in $(seq $runs); do 
  echo "Iteration $n/$runs"
  $gl --daemon -u $tasks
done
echo stop | nc localhost 18080
echo status | nc localhost 18080

cp /tmp/hp.log /tmp/hp-ref.log

$JAVA_HOME/bin/java -cp $JAVA_HOME/lib/tools.jar:$HP_HOME_DIR/honest-profiler.jar com.insightfullogic.honest_profiler.ports.console.FlameGraphDumperApplication /tmp/hp.log /tmp/flames.txt
groovy $GROOVY_SANITIZER
$FG_HOME_DIR/flamegraph.pl /tmp/flames-sanitized.txt > /tmp/$filename.svg

google-chrome /tmp/$filename.svg
