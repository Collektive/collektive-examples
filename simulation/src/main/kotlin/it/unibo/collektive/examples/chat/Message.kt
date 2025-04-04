package it.unibo.collektive.examples.chat

/**
 * Represents a message propagating in space,
 * along with its distance from the original source.
 */
data class Message(
    val content : String,
    val distance: Double
)