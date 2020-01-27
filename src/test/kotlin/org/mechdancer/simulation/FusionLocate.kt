package org.mechdancer.simulation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.mechdancer.algebra.function.vector.x
import org.mechdancer.algebra.function.vector.y
import org.mechdancer.algebra.implement.vector.vector2D
import org.mechdancer.geometry.transformation.toTransformation
import org.mechdancer.simulation.Default.newNonOmniRandomDriving
import org.mechdancer.simulation.Default.remote
import org.mechdancer.simulation.random.Normal
import kotlin.random.Random

// 机器人机械结构
private val chassis = Chassis()
private val locatorOnRobot = vector2D(-0.31, 0)

@ExperimentalCoroutinesApi
fun main() = runBlocking {
    produce {
        newNonOmniRandomDriving().run {
            while (true) {
                send(next())
                delay(100L)
            }
        }
    }.consumeEach { v ->
        //  计算机器人位姿增量
        val (_, robotOnOdometry) = chassis.drive(v)
        val locatorOnOdometry = robotOnOdometry.toTransformation()(locatorOnRobot)
        robotOnOdometry.also { (p, d) ->
            remote.paint("chassis", p.x, p.y, d.asRadian())
        }
        if (Random.nextDouble() > 0.5)
            locatorOnOdometry.run {
                remote.paint("locator",
                             x + Normal.next(.0, 0.02),
                             y + Normal.next(.0, 0.02))
            }
    }
}
