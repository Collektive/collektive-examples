package it.unibo.collektive.examples.utils

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.alchemist.model.Position
import it.unibo.collektive.stdlib.util.Point2D

/**
 * Converts an Alchemist [Position] to a [Point2D].
 */
fun Position<*>.toPoint2D(): Point2D = Point2D(coordinates[0] to coordinates[1])

/**
 * Gets the current position of the device in the environment as a [Point2D].
 */
fun CollektiveDevice<*>.coordinates(): Point2D = environment.getPosition(node).toPoint2D()

/**
 * Moves the device in the environment towards a given [direction].
 * The new position is calculated by adding the [direction] vector (multiplied by a constant [velocity])
 * to the current position.
 */
fun CollektiveDevice<*>.move(direction: Vector2D, velocity: Double) {
    val newPos = coordinates() + (direction * velocity)
    environment.moveNodeToPosition(node, environment.makePosition(newPos.x, newPos.y))
}
