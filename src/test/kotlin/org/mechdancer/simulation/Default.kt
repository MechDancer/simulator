package org.mechdancer.simulation

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
    fun newRandomDriving() =
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
}
