package org.mechdancer.simulation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import org.mechdancer.algebra.function.vector.component1
import org.mechdancer.algebra.function.vector.component2
import org.mechdancer.algebra.implement.vector.vector2DOf
import org.mechdancer.common.Odometry.Companion.pose
import org.mechdancer.common.Stamped
import org.mechdancer.common.Velocity.Companion.velocity
import org.mechdancer.common.invoke
import org.mechdancer.common.toTransformation
import org.mechdancer.simulation.Default.newOmniRandomDriving

private const val T0 = 0L
private val targetOnPerson = vector2DOf(-.5, 0)

@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val behavior = newOmniRandomDriving()
    val person = Chassis(Stamped(T0, pose()))
    val robot = Chassis(Stamped(T0, pose()))
    speedSimulation(speed = -50) {
        behavior.next()
    }.consumeEach { (t, v) ->
        val (_, personOnMap) = person.drive(v, t)
        val mapToRobot = robot[t].toTransformation().inverse()
        val personOnRobot = mapToRobot(personOnMap)
        val personToRobot = personOnRobot.toTransformation()
        val (x, y) = personToRobot(targetOnPerson)
        robot.drive(velocity(.01 * x, .01 * y, .01 * personOnRobot.d.asRadian()), t)
        println("$x, $y, ${personOnRobot.d}")
    }
}
