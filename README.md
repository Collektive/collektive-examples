# Collektive Examples

This repository contains the code for the examples of the [Collektive](https://github.com/Collektive/collektive) DSL.

## How to run the examples

To run the examples, you need to clone this repository on your pc, moving into the root folder and running the following command:

```bash
./gradlew run<ExampleName>Batch
```

Where `<ExampleName>` is the name of the example you want to run in batch mode(FieldEvolution, NeighborCounter, Branching, Gradient or ChannelWithObstacles).

## Running graphical simulations

It is possible to run also the graphical simulations with the [Alchemist simulator](https://alchemistsimulator.github.io).
You can list the available simulations by running the following command:
```bash
./gradlew tasks --all
```
And it will list all the available tasks, including the ones for the graphical simulations in the section "Run Alchemist tasks", or:
```bash
./gradlew run<ExampleName>Graphic
```
Where `<ExampleName>` is the name of the example you want to run (FieldEvolution, NeighborCounter, Branching, Gradient or ChannelWithObstacles).

