package org.mechdancer.simulation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.mechdancer.simulation.prefabs.OneStepTransferRandomDrivingBuilderDSL.Companion.oneStepTransferRandomDriving

@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val chassis = Chassis()
    val driving = oneStepTransferRandomDriving {
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

    produce {
        while (true) {
            send(driving.next())
            delay(100L)
        }
    }.consumeEach { v ->
        chassis.drive(v)
            .also { (_, pose) -> Default.remote.paint("pose", pose.p.x, pose.p.y, pose.d.asRadian()) }
            .let(::println)
    }
}
