package org.mechdancer.simulation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import org.mechdancer.common.Stamped
import org.mechdancer.simulation.Default.newOmniRandomDriving

@ExperimentalCoroutinesApi
fun main() = runBlocking<Unit> {
    val behavior = newOmniRandomDriving()
//    val person = Chassis(Stamped())
    val robot = Chassis()
    speedSimulation {
        behavior.next()
    }.consumeEach {
        println(it)
    }
}
