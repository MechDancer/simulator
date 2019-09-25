package org.mechdancer.simulation

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import org.mechdancer.common.Stamped
import org.mechdancer.common.Velocity
import org.mechdancer.common.Velocity.NonOmnidirectional
import org.mechdancer.dependency.must
import org.mechdancer.remote.modules.multicast.multicastListener
import org.mechdancer.remote.presets.remoteHub
import org.mechdancer.remote.protocol.SimpleInputStream
import org.mechdancer.remote.resources.MulticastSockets
import org.mechdancer.remote.resources.Networks
import org.mechdancer.simulation.prefabs.OneStepTransferRandomDrivingBuilderDSL.Companion.oneStepTransferRandomDriving
import java.io.DataInputStream
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

object Default {
    private val commands_ = Channel<NonOmnidirectional>(Channel.CONFLATED)
    val commands: ReceiveChannel<NonOmnidirectional> get() = commands_

    val remote by lazy {
        remoteHub("simulator") {
            inAddition {
                multicastListener { _, _, payload ->
                    if (payload.size == 16)
                        GlobalScope.launch {
                            val stream = DataInputStream(SimpleInputStream(payload))
                            @Suppress("BlockingMethodInNonBlockingContext")
                            commands_.send(Velocity.velocity(stream.readDouble(), stream.readDouble()))
                        }
                }
            }
        }.apply {
            openAllNetworks()
            println("simulator open ${components.must<Networks>().view.size} networks on ${components.must<MulticastSockets>().address}")
            thread(isDaemon = true) { while (true) invoke() }
        }
    }

    //    - 0 +
    // -  x x x
    // 0  x x x
    // +  x x x
    fun newOmniRandomDriving() =
        oneStepTransferRandomDriving {
            vx(0.1) {
                row(-1, 0.20, 0.00)
                row(0.02, -1, 0.18)
                row(0.00, 0.20, -1)
            }
            vy(0.1) {
                row(-1, 0.20, 0.00)
                row(0.02, -1, 0.18)
                row(0.00, 0.20, -1)
            }
            w(0.5) {
                row(-1, 0.10, 0.00)
                row(0.05, -1, 0.05)
                row(0.00, 0.10, -1)
            }
        }

    fun newNonOmniRandomDriving() =
        oneStepTransferRandomDriving {
            vx(0.1) {
                row(-1, .99, .00)
                row(.00, -1, .02)
                row(.00, .01, -1)
            }
            w(0.5) {
                row(-1, .01, .01)
                row(.01, -1, .01)
                row(.01, .01, -1)
            }
        }

    // 倍速仿真
    @ExperimentalCoroutinesApi
    fun <T> speedSimulation(
        scope: CoroutineScope,
        t0: Long = 0,
        dt: Long = 5L,
        speed: Int = 1,
        block: () -> T
    ) =
        when {
            speed > 0 -> scope.produce {
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
            speed < 0 -> scope.produce {
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
}
