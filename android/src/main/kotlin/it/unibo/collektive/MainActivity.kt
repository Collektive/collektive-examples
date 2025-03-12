package it.unibo.collektive

import android.content.Context
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
import it.unibo.collektive.network.BluetoothMailbox
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
            AppLauncher(lifecycleScope, this)
        }
    }
}

@Composable
private fun AppLauncher(scope: CoroutineScope, context: Context) {
    AndroidView({ View(it).apply { keepScreenOn = true } })
    val startOnClick: () -> Unit = {
        scope.launch { startAggregateProgram(context, this) }
    }
    Button(onClick = startOnClick) {
        Text(text = "Hello Collektive!")
    }
}

private suspend fun startAggregateProgram(context: Context, scope: CoroutineScope) {
    val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    val deviceId = (1..10)
        .map { Random.nextInt(0, charPool.size).let { charPool[it] } }
        .joinToString("")
//    val network = MqttMailbox(Random.nextInt(), "broker.hivemq.com", dispatcher = Dispatchers.IO)
    val network = BluetoothMailbox(deviceId, context, scope)
    var lastState = emptyMap<Path, Any?>()
    while (true) {
        val result = aggregateProgram(deviceId, network, lastState)
        lastState = result.newState
        delay(5.seconds)
    }
}

private fun aggregateProgram(id: String, network: Mailbox<String>, lastState: Map<Path, Any?>): AggregateResult<String, Any> =
    aggregate(id, network, lastState) {
        neighboring(localId)
    }
