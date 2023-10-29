package it.unibo.collektive

import it.unibo.collektive.aggregate.AggregateContext
import it.unibo.collektive.aggregate.AggregateResult
import it.unibo.collektive.aggregate.aggregate
import it.unibo.collektive.aggregate.ops.share
import it.unibo.collektive.alchemist.device.CollektiveDevice
import it.unibo.collektive.alchemist.device.DistanceSensor
import it.unibo.collektive.field.Field
import it.unibo.collektive.field.min
import it.unibo.collektive.state.State

class Aggregate(private val device: CollektiveDevice<*>) {
    private val nodeId = device.node.id
    private var state: Set<State<*>> = setOf()

    fun entrypoint(): AggregateResult<Double> = with(device) {
        val gradientResult = aggregate(IntId(nodeId), read(), state) {
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
fun gradient(source: Boolean) = share(Double.POSITIVE_INFINITY) { dist ->
    val paths = distances() + dist
    val minByPath = paths.min(includingSelf = false)?.value // field to map, excluding local
    when {
        source -> 0.0
        minByPath == null -> Double.POSITIVE_INFINITY
        else -> minByPath
    }
}
