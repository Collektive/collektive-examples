@file:Suppress("PackageNaming", "MagicNumber")
package it.unibo.collektive.examples.kt73131

import it.unibo.collektive.aggregate.api.Aggregate

fun Aggregate<Int>.shouldRaiseAWarning(): Unit {
    align(0)
    for (i in 0 until 10) {
        neighboring(i)
    }
    dealign()
}
