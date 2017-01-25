# Large Native Performance Reproduction Project

Currently, this doesn't look like a very large project. But, it is. We
just have to take a few steps to generate the sources from scratch
while we are working out whatever inaccuracies there might have been
in our code generation scripts.

## Getting Started

Clone the repo:
```sh
$> git clone https://github.com/gradle/perf-native-large.git
$> cd perf-native-large
```

From here you need to run the generation script. It takes about 30 seconds to do it's work.
```sh
$> cd generator
$> ./gradlew run
```

What it's doing is populating 430 project directories in the root of
this repository and the corresponding `settings.gradle` file which
ties the projects together in one big happy multiproject build.

Specifically, it is parsing the
[components.txt](generator/components.txt) file and generating c++
source code, header files and references to prebuit native libraries
on your platform. For each othese projects and all their complicated
interdependencies.

Speaking of prebuilt libraries. We need to *trick* the top-level build
into thinking that there are some prebuilt binaries, so we can
satisfiy the needs of the generated `:project431` which has all ton of
`PrebuiltLibrary` entries.

Do do that run:
```sh
$> cd ../prebuilt/util
$> ./gradlew assemble
```

At this point, you should be able to pop back up to the top level and run the build.

```sh
$> cd ../
$> gradle assemble
```

*Note* I haven't added a top-level `build.gradle` file yet because I
 didn't want people to see it in the repo and think this project would
 behave just like any other one you've used before.

## Integrated profiling tools

### Profiling with gradle-profiler

Use the Gradle profiler to `--benchmark` or `--profile` scenarios. The available scenarios are defined in `performance.scenarios`

Example usage: `./gradle-profiler --profile chrome-trace upToDateAssemble`

### Profiling with honest-profiler

There is a script [`prof.sh`](profiler/prof.sh) which automates running honest-profiler and creating a flamegraph of the execution.

These environment variables must be set to run the script:
- `JAVA_HOME` : path to the JDK
- `HP_HOME_DIR` : path to an installation of [honest-profiler](https://github.com/RichardWarburton/honest-profiler). This must be compiled from sources since it requires some changes that are in master branch.
- `FG_HOME_DIR` : path to an installation of [FlameGraph](https://github.com/brendangregg/FlameGraph). This is a clone of the [FlameGraph repository](https://github.com/brendangregg/FlameGraph).
- `GRADLE_BIN` : path to the gradle binary which will be used to execute the builds. Could use `GRADLE_BIN=./gradlew` to use wrapper.

Example command
`GRADLE_BIN=~/.sdkman/gradle/3.1-rc-1/bin/gradle ./profiler/prof.sh assemble`


### Profiling with Java Flight Recorder

There is a script [`jfr_prof.sh`](profiler/jfr_prof.sh) which automates running a build with Java Flight Recorder enabled.

These environment variables must be set to run the script:
- `JAVA_HOME` : path to the JDK
- `GRADLE_BIN` : path to the gradle binary which will be used to execute the builds. Could use `GRADLE_BIN=./gradlew` to use wrapper.

Example command
`GRADLE_BIN=~/.sdkman/gradle/3.1-rc-1/bin/gradle ./profiler/jfr_prof.sh assemble`

JFR dump files will be opened with `jmc -open` when the profiling is over. 
