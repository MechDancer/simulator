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

    private const val dt = 5L

    // 倍速仿真
    @ExperimentalCoroutinesApi
    fun <T> speedSimulation(
        scope: CoroutineScope,
        t0: Long = 0,
        speed: Int = 1,
        block: () -> T
    ) =
        scope.produce {
            // 仿真时间
            var time = t0
            while (true) {
                time += dt * speed
                send(Stamped(time, block()))
                delay(dt)
            }
        }
}
