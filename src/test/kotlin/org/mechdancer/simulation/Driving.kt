package org.mechdancer.simulation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.mechdancer.simulation.Default.newOmniRandomDriving
import org.mechdancer.simulation.Default.remote

@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val chassis = Chassis()
    produce {
        newOmniRandomDriving().run {
            while (true) {
                send(next())
                delay(100L)
            }
        }
    }.consumeEach { v ->
        chassis.drive(v)
            .also { (_, pose) -> remote.paint("pose", pose.p.x, pose.p.y, pose.d.asRadian()) }
            .let(::println)
    }
}
