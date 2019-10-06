package org.mechdancer.simulation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import org.mechdancer.common.Stamped
import kotlin.system.measureTimeMillis

// 倍速仿真
@ExperimentalCoroutinesApi
fun <T> CoroutineScope.speedSimulation(
    t0: Long = 0,
    dt: Long = 5L,
    speed: Int = 1,
    block: () -> T
) =
    when {
        speed > 0 -> produce {
            // 仿真时间
            var time = t0
            while (true) {
                val cost = measureTimeMillis {
                    time += dt * speed
                    send(Stamped(time, block()))
                }
                if (dt > cost) delay(dt - cost)
            }
        }
        speed < 0 -> produce {
            // 仿真时间
            var time = t0
            while (true) {
                time += dt
                send(Stamped(time, block()))
                delay(dt * -speed)
            }
        }
        else      -> throw IllegalArgumentException("speed cannot be zero")
    }
