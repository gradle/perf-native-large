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

From here you need to run the generation script. It takes about 4 minutes to do it's work.
```sh
$> cd generator
$> ./gradlew run
```

What it's doing is populating 430 project directories in the root of
this repository and the corresponding `settings.gradle` file which
ties the projects together in one big happy multiproject build.

Specifically, it is parsing the
[customers.txt](generator/customers.txt) file and generating c++
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
