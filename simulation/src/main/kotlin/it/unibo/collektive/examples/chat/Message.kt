package it.unibo.collektive.examples.chat

/**
 * Represents a message with [content] propagating in space,
 * along with its [distanceFromSource].
 */
data class Message(val content: String, val distanceFromSource: Double)
