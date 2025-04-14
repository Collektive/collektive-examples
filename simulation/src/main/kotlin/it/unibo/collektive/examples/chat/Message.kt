package it.unibo.collektive.examples.chat

/**
 * Represents a message with [content] propagating in space,
 * along with its [distance] from the original source.
 */
interface Message {
    val content: String
    val distance: Double
}

/**
 * Builds a [Message] with appropriate [content] based on [distance].
 *
 * - If [distance] ≤ [REACHABLE], the full [content] message is returned.
 * - If [distance] < [THRESHOLD], a faded version is returned using [fadeMessage].
 * - If [distance] ≥ [THRESHOLD], the message is considered unreachable.
 */
class FadedMessage private constructor(override val content: String, override val distance: Double) : Message {
    /**
     * Companion object for factory methods.
     */
    companion object {
        /**
         * Returns a [FadedMessage] starting from a [base] content and its [distance] from source.
         */
        operator fun invoke(base: String, distance: Double): FadedMessage {
            val message = when {
                distance <= REACHABLE -> base
                distance < THRESHOLD -> fadeMessage(base, distance)
                else -> "Unreachable"
            }
            return FadedMessage(message, distance)
        }
    }

    override fun toString(): String = "FadedMessage(content='$content', distance=$distance)"
}

/**
 * Formats a faded version of a [message] based on [distance].
 *
 * The message is suffixed with a percentage representing how strongly it is perceived
 * (100% when distance equals [REACHABLE], 0% when it reaches [THRESHOLD]).
 * Used to simulate signal degradation in proximity-based messaging.
 */
private fun fadeMessage(message: String, distance: Double): String {
    val faintness = calculateFaint(distance)
    val percentage = "%.0f".format(faintness)
    return "$message $percentage%"
}

/**
 * Computes the perceived intensity (faintness) of a message based on distance.
 *
 * The result is a percentage from 100 (fully clear) to 0 (barely understandable).
 * Intended to be used when distance is between [REACHABLE] and [THRESHOLD].
 */
private fun calculateFaint(distance: Double): Double = (1.0 - ((distance - REACHABLE) / REACHABLE)) * 100
