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

fun fadingMessage(base: String, distance: Double): Message = when {
    distance <= REACHABLE -> Message(base, distance)
    distance < THRESHOLD -> Message(fadeMessage(base, distance), distance)
    else -> Message("Unreachable", distance)
}

fun fadeMessage(message: String, distance: Double): String {
    val faintness = calculateFaint(distance)
    val percentage = "%.0f".format(faintness)
    return "$message $percentage%"
}