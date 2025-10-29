package it.unibo.collektive.examples.utils

import it.unibo.collektive.stdlib.util.Point2D
import kotlin.math.sqrt

/**
 * Represents a 2D vector in Cartesian coordinates.
 * This is an alias for [Point2D] that provides vector-specific operations
 * such as normalization, magnitude calculation, and dot product.
 */
typealias Vector2D = Point2D

/**
 * Zero vector (0,0).
 */
val vectorZero = Vector2D(0.0 to 0.0)

/**
 * Normalizes the vector, returning a new vector with the same direction but with magnitude 1.
 * If the vector has a magnitude of 0, it returns a zero vector.
 */
fun Vector2D.normalize(): Vector2D = this / (magnitude().takeIf { it > 0.0 } ?: 1.0)

/**
 * Calculates the Euclidean magnitude (length) of the vector.
 */
fun Vector2D.magnitude(): Double = sqrt(x * x + y * y)

/**
 * Scalar multiplication between this and [other].
 */
infix fun Vector2D.vdot(other: Vector2D): Double = x * other.x + y * other.y
