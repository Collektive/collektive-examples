package it.unibo.alchemist.boundary.swingui.effect.impl

import it.unibo.alchemist.boundary.ui.api.Wormhole2D
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.physics.environments.Physics2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
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

/**
 * Effect that draws a unit direction vector as an arrow from the node's position.
 */
@Suppress("DEPRECATION")
class DrawDirection(private val c: Color = Color.RED) : it.unibo.alchemist.boundary.swingui.effect.api.Effect {

    override fun getColorSummary(): Color = c

    override fun <T, P : Position2D<P>> apply(
        graphics: Graphics2D,
        node: Node<T>,
        environment: Environment<T, P>,
        wormhole: Wormhole2D<P>,
    ) {
        require(environment is Physics2DEnvironment<T>) {
            "DrawDirection effect can only be applied in Physics2DEnvironment."
        }
        val direction = computeDirection(environment, node)
            .takeIf { it != Euclidean2DPosition(0.0, 0.0) }
            ?: return
        drawDirectedArrow(graphics, environment, node, wormhole, direction)
    }

    private fun <T> computeDirection(environment: Physics2DEnvironment<T>, node: Node<T>): Euclidean2DPosition =
        with(environment.getHeading(node)) {
            sqrt(x * x + y * y)
                .takeIf { it != 0.0 }
                ?.let { magnitude -> Euclidean2DPosition(x / magnitude, y / magnitude) }
                ?: Euclidean2DPosition(0.0, 0.0)
        }

    private fun <T, P : Position2D<P>> drawDirectedArrow(
        graphics: Graphics2D,
        environment: Environment<T, P>,
        node: Node<T>,
        wormhole: Wormhole2D<P>,
        direction: Euclidean2DPosition,
    ) {
        val viewPoint = wormhole.getViewPoint(environment.getPosition(node))
        val arrow = createArrowPath(direction)
        val transform = with(viewPoint) {
            AffineTransform().apply {
                translate(x.toDouble(), y.toDouble())
                wormhole.zoom.let { scale(it, it) }
                scale(1.0, -1.0)
            }
        }
        renderArrow(graphics, arrow, transform)
    }

    private fun createArrowPath(direction: Euclidean2DPosition): Path2D.Double = with(direction) {
        val (endX, endY) = this * UNIT_ARROW_LENGTH
        val angle = atan2(y, x)
        return Path2D.Double().apply {
            moveTo(0.0, 0.0)
            lineTo(endX, endY)
            addArrowHeads(endX, endY, angle)
        }
    }

    private fun Path2D.Double.addArrowHeads(endX: Double, endY: Double, angle: Double) {
        sequenceOf(-ARROW_HEAD_ANGLE, ARROW_HEAD_ANGLE).forEach { offset ->
            val headAngle = angle + offset
            val headX = endX - ARROW_HEAD_LENGTH * cos(headAngle)
            val headY = endY - ARROW_HEAD_LENGTH * sin(headAngle)
            moveTo(endX, endY)
            lineTo(headX, headY)
        }
    }

    private fun renderArrow(graphics: Graphics2D, arrow: Path2D.Double, transform: AffineTransform) = graphics.apply {
        color = c
        stroke = BasicStroke(ARROW_STROKE_WIDTH)
        draw(transform.createTransformedShape(arrow))
    }

    /**
     * Contains constants for DrawDirection.
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
