package org.mechdancer.simulation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.mechdancer.algebra.implement.matrix.builder.matrix
import org.mechdancer.simulation.prefabs.RandomDriving

@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val chassis = Chassis()
    val driving = RandomDriving(
        v = 0.1,
        w = 0.5,
        vMatrix = matrix {
            row(0.80, 0.20, 0.00)
            row(0.02, 0.80, 0.18)
            row(0.00, 0.20, 0.80)
        },
        wMatrix = matrix {
            row(0.90, 0.10, 0.00)
            row(0.05, 0.90, 0.05)
            row(0.00, 0.10, 0.90)
        }
    )

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
