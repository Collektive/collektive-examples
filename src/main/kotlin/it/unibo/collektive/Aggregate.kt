package it.unibo.collektive

import it.unibo.collektive.alchemist.device.CollektiveDevice
import it.unibo.collektive.alchemist.device.DistanceSensor
import it.unibo.collektive.field.Field
import it.unibo.collektive.field.min
import it.unibo.collektive.stack.Path

class Aggregate(private val node: CollektiveDevice<*>) {
    private val nodeId = node.node.id
    private var state = emptyMap<Path, Any?>()
    fun entrypoint() = aggregate(IntId(nodeId), node.receive(), state) {
        gradient(nodeId == 0, node)
    }.also { state = it.newState }
}

operator fun Field<Double>.plus(field: Field<Double>): Field<Double> {
    val res = (this.toMap().toList() + field.toMap().toList())
        .groupBy({ it.first }, { it.second })
        .map { (key, values) -> key to values.sum() }
        .toMap()
    return Field(this.localId, res)
}

fun AggregateContext.gradient(source: Boolean, sensor: DistanceSensor) =
    sharing(Double.POSITIVE_INFINITY) { distances ->
        val paths: Field<Double> = sensor.distances() + distances
        val minByPath = paths.min(includingSelf = false)?.value // field to map, excluding local
        when {
            source -> 0.0
            minByPath == null -> Double.POSITIVE_INFINITY
            else -> minByPath
        }
    }
