package org.mechdancer.simulation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import org.mechdancer.common.Stamped
import org.mechdancer.dependency.must
import org.mechdancer.remote.presets.remoteHub
import org.mechdancer.remote.resources.MulticastSockets
import org.mechdancer.remote.resources.Networks
import org.mechdancer.simulation.prefabs.OneStepTransferRandomDrivingBuilderDSL.Companion.oneStepTransferRandomDriving

object Default {
    val remote by lazy {
        remoteHub("simulator").apply {
            openAllNetworks()
            println("simulator open ${components.must<Networks>().view.size} networks on ${components.must<MulticastSockets>().address}")
        }
    }

    //    - 0 +
    // -  x x x
    // 0  x x x
    // +  x x x
    fun newOmniRandomDriving() =
        oneStepTransferRandomDriving {
            vx(0.1) {
                row(0.80, 0.20, 0.00)
                row(0.02, 0.80, 0.18)
                row(0.00, 0.20, 0.80)
            }
            vy(0.1) {
                row(0.80, 0.20, 0.00)
                row(0.02, 0.80, 0.18)
                row(0.00, 0.20, 0.80)
            }
            w(0.5) {
                row(0.90, 0.10, 0.00)
                row(0.05, 0.90, 0.05)
                row(0.00, 0.10, 0.90)
            }
        }

    fun newNonOmniRandomDriving() =
        oneStepTransferRandomDriving {
            vx(0.1) {
                row(0.99, 0.01, 0.00)
                row(0.00, 0.96, 0.04)
                row(0.00, 0.01, 0.99)
            }
            w(0.5) {
                row(0.90, 0.10, 0.00)
                row(0.02, 0.96, 0.02)
                row(0.00, 0.10, 0.90)
            }
        }

    // 倍速仿真
    @ExperimentalCoroutinesApi
    fun <T> speedSimulation(
        scope: CoroutineScope,
        t0: Long,
        speed: Long,
        block: () -> T
    ) =
        scope.produce {
            // 仿真时间
            var time = t0
            while (true) {
                val value = block()
                for (i in 0 until speed) {
                    time += speed
                    this.send(Stamped(time, value))
                    delay(1L)
                }
            }
        }
}
