# Alchemist Primer

This is a template project to spawn projects using the [Alchemist Simulator](https://github.com/AlchemistSimulator/Alchemist).
It provides a pre-configured gradle build.

This project is a quick start for the [Alchemist](https://github.com/AlchemistSimulator/Alchemist) simulator, it shows how to use the simulator via [Gradle](https://gradle.org) to run a simple simulation. More information can be found on [the official Alchemist website](https://alchemistsimulator.github.io).

## Prerequisites

A [Gradle-compatible Java version](https://docs.gradle.org/current/userguide/compatibility.html).

## How to launch

To run the example you can rely on the pre-configured [Gradle](https://gradle.org) build script.
It will automatically download all the required libraries, set up the environment, and execute the simulator via command line for you.
As first step, use `git` to locally clone this repository.

Simulations can be included in the `src/main/yaml` folder,
and executed via the `runAll` Gradle task.

For each YAML file in `src/main/yaml` a task `runFileName` will be created.

In order to launch, open a terminal and move to the project root folder, then on UNIX:
```bash
./gradlew runAll
```
On Windows:
```
gradlew.bat runAll
```

Press <kb>P</kb> to start the simulation.
For further information about the gui, see the [Alchemist documentation](https://alchemistsimulator.github.io/).

Note that the first launch will take some time, since Gradle will download all the required files.
They will get cached in the user's home folder (as per Gradle normal behavior).

## The build script

Let's explain how things work by looking at the `build.gradle.kts` script and the `gradle/libs.versions.toml` file.
The latter is a [Gradle dependency catalog file](https://docs.gradle.org/current/userguide/platforms.html#sub:conventional-dependencies-toml)
including the specification of the required libraries,
including their name, their version, and how they get bundled;
while the former is the actual script that launches the simulator.

The build script imports the bundled alchemist dependencies as:
```kotlin
dependencies {
    implementation(libs.bundles.alchemist)
}
```

You will either need to import the full version of alchemist (but it's not recommended, as it pulls in a lot of dependencies you probably don't want),
or manually declare which modules you want to include into your Alchemist simulations.

At the very least, you want to pull in an incarnation.
This can be done directly in the build file, or (recommended way) through the TOML catalog.

For instance,
let's say that you want to pull in the module for simulating on real-world maps,
and that you also need a library named `bar` at version `1.2.3`, from group `io.github.foo`, available on [Maven Central](https://search.maven.org/).
You could import both of them by changing the catalog as follows:

```toml
[versions]
alchemist = "<the version of alchemist is here>"

[libraries]
alchemist = { module = "it.unibo.alchemist:alchemist", version.ref = "alchemist" }
alchemist-incarnation-protelis = { module = "it.unibo.alchemist:alchemist-incarnation-protelis", version.ref = "alchemist" }
alchemist-swingui = { module = "it.unibo.alchemist:alchemist-swingui", version.ref = "alchemist" }
# The following two lines are additions, the first defines the maps module of alchemist...
alchemist-maps = { module = "it.unibo.alchemist:alchemist-maps", version.ref = "alchemist" }
# ...the second the custom library with a more compact syntax as there is no need to reuse the version defined above.
foobar = "io.github.foo:bar:1.2.3"

[bundles]
alchemist = [
    "alchemist",
    "alchemist-incarnation-protelis",
    "alchemist-swingui",
    # Adding the newly defined libraries to the alchemist bundle makes them available automatically
    "alchemist-maps",
    "foobar"
]

```

The build script defines some tasks meant to run Alchemist from the command line through Gradle with ease.
There will be one task for each simulation file in `src/main/yaml`, dynamically detected and prepared by Gradle.
You can see a summary of the tasks for running Alchemist by issuing
`./gradlew tasks --all`
(or `gradlew.bat tasks --all` under Windows)
and looking for the `Run Alchemist` task group.

Internally, the task relies on the Alchemist command-line interface.
Alchemist simulations are [YAML](https://yaml.org/spec/) files,
more information about them can be found [here](https://alchemistsimulator.github.io/).
