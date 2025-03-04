package it.unibo.collektive

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import it.unibo.collektive.Collektive.Companion.aggregate
import it.unibo.collektive.aggregate.AggregateResult
import it.unibo.collektive.aggregate.api.Aggregate.Companion.neighboring
import it.unibo.collektive.network.MqttMailbox
import it.unibo.collektive.networking.Mailbox
import it.unibo.collektive.path.Path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppLauncher(lifecycleScope)
        }
    }
}

@Composable
private fun AppLauncher(scope: CoroutineScope) {
    AndroidView({ View(it).apply { keepScreenOn = true } })
    val startOnClick: () -> Unit = {
        scope.launch { startAggregateProgram() }
    }
    Button(onClick = startOnClick) {
        Text(text = "Hello Collektive!")
    }
}

private suspend fun startAggregateProgram() {
    val network = MqttMailbox(Random.nextInt(), "broker.hivemq.com", dispatcher = Dispatchers.IO)
    var lastState = emptyMap<Path, Any?>()
    while (true) {
        val result = aggregateProgram(Random.nextInt(), network, lastState)
        lastState = result.newState
        delay(5.seconds)
    }
}

private fun aggregateProgram(
    id: Int,
    network: Mailbox<Int>,
    lastState: Map<Path, Any?>,
): AggregateResult<Int, Any> =
    aggregate(id, network, lastState) {
        neighboring(localId)
    }
