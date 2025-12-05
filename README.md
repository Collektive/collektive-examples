# Collektive Examples

This repository contains the code for the examples of the [Collektive](https://github.com/Collektive/collektive) DSL.

### Table of Contents
- [Getting Started](#getting-started)
    - [Requirements](#requirements)
    - [Limitations](#limitations)
    - [Reproduce the entire experiment](#reproduce-the-entire-experiment)
        * [Simulation Graphical Interface](#simulation-graphical-interface)
        * [Extremely quick-start of a basic experiment -- `(ba|z|fi)?sh` users only](#extremely-quick-start-of-a-basic-experiment----bazfish-users-only)
        * [Reproduce the experiments through Gradle](#reproduce-the-experiments-through-gradle)
        * [Changing experiment's parameters](#changing-experiments-parameters)
    - [Reproduce the experiment results](#reproduce-the-experiment-results)
        * [Reproduce the experiments with containers (recommended)](#reproduce-the-experiments-with-containers-recommended)
        * [Reproduce natively](#reproduce-natively)
        * [Generate the charts](#generate-the-charts)

    
## Getting started

### Requirements

In order to successfully download and execute the graphical experiments are needed:
- Internet connection;
- [Git](https://git-scm.com);
- Linux, macOS and Windows systems capable of running [Java](https://www.oracle.com/java/technologies/javase/jdk19-archive-downloads.html) 17 (or higher);
- 4GB free space on disk (the system will automatically download its dependencies through Gradle);
- GPU with minimal OpenGL capabilities (OpenGL 2.0);
- 4GB RAM.

The project uses [Gradle](https://gradle.org) as a build tool,
and all the project dependencies are listed in the `gradle\libs.versions.toml` file.

### Limitations

- The experiments run in "batch mode" generate a lot of data,
  and the simulation may take a long time to finish (up to several hours) even with high-performance computers.
  We suggest running the experiments in "graphic mode" to have a better understanding of the simulation;
- On different monitor types with different resolutions, the graphical interface could appear a bit different;
- "batch mode" does not show any graphical interface;
- For GUI interpretation, please refer to the [Simulation Graphical Interface](#simulation-graphical-interface) section.

### Reproduce the entire experiment

**WARNING**: re-running the whole experiment may take a very long time on a normal computer.

#### Simulation Graphical Interface

The simulation environment and graphical interface are provided by [Alchemist Simulator](https://alchemistsimulator.github.io/index.html).
To understand how to interact with the GUI,
please refer to the [Alchemist documentation](https://alchemistsimulator.github.io/reference/swing/index.html#shortcuts).

#### Extremely quick-start of a basic experiment -- `(ba|z|fi)?sh` users only

- Requires a Unix terminal (`(ba|z|fi)?sh`)
- `curl` must be installed
- run:
```bash
# gradient example
curl https://raw.githubusercontent.com/collektive/collektive-examples/master/run-gradient.sh | bash 
```
- the repository is in your `Downloads` folder for further inspection.

#### Reproduce the experiments through Gradle

1. Install a Gradle-compatible version of Java.
   Use the [Gradle/Java compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html) to learn which is the compatible version range.
   The Version of Gradle used in this experiment can be found in the gradle-wrapper.properties file located in the gradle/wrapper folder.
2. Open a terminal
3. Clone this repository on your pc with `git clone https://github.com/collektive/collektive-examples`.
4. Move into the root folder with `cd collektive-examples`
5. Depending on the platform, run the following command:
    - Bash compatible (Linux, Mac OS X, Git Bash, Cygwin): ``` ./gradlew run<ExperimentName>Graphic ```
    - Windows native (cmd.exe, Powershell): ``` gradlew.bat run<ExperimentName>Graphic ```
6. Substitute `<ExperimentName>` with the name of the experiment (in PascalCase) specified in the YAML simulation file.
   Or execute ```./gradlew tasks``` to view the list of available tasks.

**NOTES:**
- Due to Alchemist's limitations, the graphical interface will not appear if run on a docker container.
- The tasks *in graphic mode* will run the experiments with the default parameters.

#### Changing experiment's parameters
To change the parameters of the experiments, you can modify the **YAML** files located in the `src/main/yaml` folder.

Each change in the parameters will result in a different setup and execution of the experiment.

For further information about the YAML structure,
please refer to the [Alchemist documentation](https://alchemistsimulator.github.io/reference/yaml/index.html).

### Reproduce the experiment results

**WARNING**: re-running the whole experiment may take a very long time on a normal computer.

To collect the data for the analysis and the charts,
the experiments have to be run in "batch mode,"
which means that the experiments are run without the graphical interface,
and with different combinations of parameters.
Running the experiments in batch mode in a normal computer may take a very long time (e.g., days),
depending on the available hardware.
Moreover, 
be sure that the YAML file of the experiment you want to run in batch mode
is properly configured with the desired parameters.

#### Reproduce the experiments with containers (recommended)

**NOT YET SUPPORTED BY DEFAULT, WILL BE AVAILABLE SOON**, but if you want to try it out, follow the steps below:

1. Install [Docker](https://www.docker.com/products/docker-desktop) and [docker-compose](https://docs.docker.com/compose/install/);
2. Run `docker-compose up` in the root folder of the repository:
   this will build the Docker images and run the containers needed to run the experiments.
3. From the `docker-compose.yml` file, you can see that eight separate containers will be created, one for each experiment, and the data will be collected in the `data` folder.
   Note that the `volumes` field has to be updated to match your local environment.

#### Reproduce natively

1. Install a Gradle-compatible version of Java.
   Use the [Gradle/Java compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html)
   to learn which is the compatible version range.
   The Version of Gradle used in this experiment can be found in the `gradle-wrapper.properties` file
   located in the `gradle/wrapper` folder.
2. Install the version of Python indicated in `.python-version` (or use `pyenv`).
3. Launch either:
    - `./gradlew runAllBatch` on Linux, MacOS, or Windows if a bash-compatible shell is available;
    - `gradlew.bat runAllBatch` on Windows cmd or Powershell;
4. Once the experiment is finished, the results will be available in the `data` folder, 
**if data extraction is properly configured in the YAML files**.

#### Generate the charts

**NOT YET SUPPORTED BY DEFAULT, WILL BE AVAILABLE SOON**, but if you want to try it out, follow the steps below:

**WARNING**: depending on the amount of data collected, this process may take a long time.

1. Make sure you have Python 3.10 or higher installed.
2. The data folder structure should be the following:
    ```txt
    collektive-examples/
    ├── data/
    │   ├── <experiment-name>/
    │   ├── <experiment-name2>/
    │   └── .../
    ```
3. Install the required Python packages by running:
    ```bash
    pip install --upgrade pip
    pip install -r requirements.txt
    ```
4. Run the script to process the data and generate the charts (this process may take some time):
    ```bash
    python plotter_all.py
    ```
5. The charts will be generated in the `charts` folder.
6. If you want to regenerate the charts, you can run the script again.