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

/**
 * Builds a [Message] with appropriate content based on [distance].
 *
 * - If [distance] ≤ [REACHABLE], the full [base] message is returned.
 * - If [distance] < [THRESHOLD], a faded version is returned using [fadeMessage].
 * - If [distance] ≥ [THRESHOLD], the message is considered unreachable.
 */
fun fadingMessage(base: String, distance: Double): Message = when {
    distance <= REACHABLE -> Message(base, distance)
    distance < THRESHOLD -> Message(fadeMessage(base, distance), distance)
    else -> Message("Unreachable", distance)
}

/**
 * Formats a faded version of a [message] based on [distance].
 *
 * The message is suffixed with a percentage representing how strongly it is perceived
 * (100% when distance equals [REACHABLE], 0% when it reaches [THRESHOLD]).
 * Used to simulate signal degradation in proximity-based messaging.
 */
fun fadeMessage(message: String, distance: Double): String {
    val faintness = calculateFaint(distance)
    val percentage = "%.0f".format(faintness)
    return "$message $percentage%"
}