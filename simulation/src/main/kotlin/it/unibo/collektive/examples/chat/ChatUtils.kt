package it.unibo.collektive.examples.chat

/**
 * Computes the perceived intensity (faintness) of a message based on distance.
 *
 * The result is a percentage from 100 (fully clear) to 0 (barely understandable).
 * Intended to be used when distance is between [REACHABLE] and [THRESHOLD].
 */
fun calculateFaint(distance: Double):Double{
    return (1.0 - (distance - REACHABLE)/ REACHABLE)*100
}