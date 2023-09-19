package it.unibo.collektive

import it.unibo.collektive.alchemist.device.CollektiveDevice
import it.unibo.collektive.alchemist.device.DistanceSensor
import it.unibo.collektive.field.Field
import it.unibo.collektive.field.min
import it.unibo.collektive.stack.Path

class Aggregate(private val device: CollektiveDevice<*>) {
    private val nodeId = device.node.id
    private var state = emptyMap<Path, Any?>()

    fun entrypoint(): AggregateContext.AggregateResult<Double> = with(device) {
        val gradientResult = aggregate(IntId(nodeId), receive(), state) {
            gradient(nodeId == 0)
        }
        state = gradientResult.newState
        gradientResult
    }
}

operator fun Field<Double>.plus(field: Field<Double>): Field<Double> {
    val res = (this.toMap().toList() + field.toMap().toList())
        .groupBy({ it.first }, { it.second })
        .map { (key, values) -> key to values.sum() }
        .toMap()
    return Field(this.localId, res)
}

context(AggregateContext, DistanceSensor)
fun gradient(source: Boolean) = sharing(Double.POSITIVE_INFINITY) { dist ->
    val paths = distances() + dist
    val minByPath = paths.min(includingSelf = false)?.value // field to map, excluding local
    when {
        source -> 0.0
        minByPath == null -> Double.POSITIVE_INFINITY
        else -> minByPath
    }
}
