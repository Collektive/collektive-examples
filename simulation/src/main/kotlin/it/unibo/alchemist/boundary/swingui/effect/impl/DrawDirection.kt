package it.unibo.alchemist.boundary.swingui.effect.impl

import it.unibo.alchemist.boundary.ui.api.Wormhole2D
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.physics.environments.Physics2DEnvironment
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.geom.Path2D
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.times
import kotlin.unaryMinus

/**
 * Effect that draws a unit direction vector as an arrow from the node's position.
 */
@Suppress("DEPRECATION")
class DrawDirection @JvmOverloads constructor(
    private val c: Color = Color.RED,
    private val unitLength: Double = UNIT_ARROW_LENGTH,
) : it.unibo.alchemist.boundary.swingui.effect.api.Effect {

    override fun <T, P : Position2D<P>> apply(
        graphics: Graphics2D,
        node: Node<T>,
        environment: Environment<T, P>,
        wormhole: Wormhole2D<P>,
    ) {
        if (environment is Physics2DEnvironment<*>) {
            @Suppress("UNCHECKED_CAST")
            drawDirectionArrow(
                graphics,
                node,
                environment as Physics2DEnvironment<T>,
                wormhole,
            )
        }
    }

    override fun getColorSummary(): Color = c

    private fun <T, P : Position2D<P>> drawDirectionArrow(
        graphics: Graphics2D,
        node: Node<T>,
        environment: Physics2DEnvironment<T>,
        wormhole: Wormhole2D<P>,
    ) {
        val zoom = wormhole.zoom
        val pos = environment.getPosition(node)

        @Suppress("UNCHECKED_CAST")
        val viewPoint = wormhole.getViewPoint(pos as P)
        val x = viewPoint.x
        val y = viewPoint.y
        // Get the direction vector
        val direction = environment.getHeading(node)
        val magnitude = sqrt(direction.x * direction.x + direction.y * direction.y)
        if (magnitude == 0.0) {
            return // Skip nodes with zero heading
        }
        // Normalize to unit vector
        val normX = direction.x / magnitude
        val normY = direction.y / magnitude
        // Draw unit vector with fixed length
        val endX = normX * unitLength
        val endY = normY * unitLength
        // Set arrow shape
        val arrow = Path2D.Double().apply {
            moveTo(0.0, 0.0)
            lineTo(endX, endY)
            val angle = atan2(normY, normX)

            // Set arrow head
            fun addArrowHead(angleOffset: Double) {
                val headX = endX - ARROW_HEAD_LENGTH * cos(angle + angleOffset)
                val headY = endY - ARROW_HEAD_LENGTH * sin(angle + angleOffset)
                moveTo(endX, endY)
                lineTo(headX, headY)
            }
            addArrowHead(-ARROW_HEAD_ANGLE)
            addArrowHead(ARROW_HEAD_ANGLE)
        }
        // Transform to screen coordinates
        val transform = AffineTransform().apply {
            translate(x.toDouble(), y.toDouble())
            scale(zoom, zoom)
            scale(1.0, -1.0)
        }
        val transformedArrow = transform.createTransformedShape(arrow)
        // Draw the direction
        graphics.color = c
        graphics.stroke = BasicStroke(ARROW_STROKE_WIDTH)
        graphics.draw(transformedArrow)
    }

    /**
     * Companion object for constants.
     */
    companion object {
        @JvmStatic
        private val serialVersionUID = 1L
        private const val UNIT_ARROW_LENGTH = 10.0
        private const val ARROW_HEAD_LENGTH = 5.0
        private const val ARROW_HEAD_ANGLE = PI / 6
        private const val ARROW_STROKE_WIDTH = 2.0f
    }
}
