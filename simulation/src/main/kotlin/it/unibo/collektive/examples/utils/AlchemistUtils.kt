package it.unibo.collektive.examples.utils

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.physics.environments.Physics2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
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

/**
 * Sets the heading of the device in the environment towards a given [direction].
 * The environment where the device is located must be a [Physics2DEnvironment].
 */
fun CollektiveDevice<*>.pointTo(direction: Vector2D) = (environment as? Physics2DEnvironment<Any?>)
    ?.setHeading(node, direction.toEuclidean2DPosition())

/**
 * Converts a [Vector2D] to an [Euclidean2DPosition].
 */
fun Vector2D.toEuclidean2DPosition(): Euclidean2DPosition = Euclidean2DPosition(x, y)
