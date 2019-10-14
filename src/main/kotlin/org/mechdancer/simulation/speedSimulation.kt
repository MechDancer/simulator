package org.mechdancer.simulation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import org.mechdancer.common.Stamped
import java.math.BigDecimal
import java.text.DecimalFormat
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.system.measureTimeMillis

/** 倍速仿真 */
@ExperimentalCoroutinesApi
fun <T> CoroutineScope.speedSimulation(
    t0: Long = 0,
    dt: Long = 20L,
    speed: Int = 1,
    block: (Long) -> T
) =
    produce(capacity = Channel.CONFLATED) {
        var time = t0
        when {
            speed > 0 -> while (true) {
                val cost = measureTimeMillis {
                    time += dt * speed
                    send(Stamped(time, block(time)))
                }
                if (dt > cost) delay(dt - cost)
            }
            speed < 0 -> while (true) {
                val cost = measureTimeMillis {
                    time += dt
                    send(Stamped(time, block(time)))
                }
                delay(dt * -speed - cost)
            }
            else      -> throw IllegalArgumentException("speed cannot be zero")
        }
    }

@ExperimentalCoroutinesApi
suspend fun <T> Sequence<Stamped<T>>.play(
    context: CoroutineContext = EmptyCoroutineContext,
    speed: Double = 1.0
) =
    GlobalScope.produce(context) {
        var last = Long.MAX_VALUE
        for ((t, value) in this@play) {
            ((t - last) / speed)
                .toLong()
                .takeIf { it > 10 }
                ?.also { delay(it) }
            last = t
            send(Stamped(t, value))
        }
    }

// 显示格式
private val format = DecimalFormat("0.000")

/** 显示格式化信息到控制台 */
fun displayOnConsole(vararg entry: Pair<String, Number>) =
    entry.joinToString(" | ") { (key, value) ->
        when (value) {
            is Float, is Double, is BigDecimal -> "$key = ${format.format(value)}"
            else                               -> "$key = $value"
        }
    }.let(::println)
