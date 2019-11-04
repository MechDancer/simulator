package org.mechdancer.common.shape

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import org.mechdancer.algebra.implement.vector.to2D
import org.mechdancer.algebra.implement.vector.vector2DOf
import org.mechdancer.common.Odometry
import org.mechdancer.common.Stamped
import org.mechdancer.common.Velocity
import org.mechdancer.common.Velocity.NonOmnidirectional
import org.mechdancer.common.toTransformation
import org.mechdancer.geometry.angle.toDegree
import org.mechdancer.geometry.angle.toRad
import org.mechdancer.simulation.*
import org.mechdancer.simulation.Default.remote
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.PI

@ExperimentalCoroutinesApi
fun main() = runBlocking(Dispatchers.Default) {
    val obstacles = listOf(
        Polygon(listOf(
            vector2DOf(-1, +1),
            vector2DOf(-5, +1),
            vector2DOf(-5, +5),
            vector2DOf(-1, +5))),
        Polygon(listOf(
            vector2DOf(-1, -2),
            vector2DOf(-3, -1),
            vector2DOf(-2, -6))),
        Polygon(listOf(
            vector2DOf(+3, +10),
            vector2DOf(+4, +10),
            vector2DOf(+4, -10),
            vector2DOf(+3, -10))))
    val chassis = Chassis(Stamped(0L, Odometry.pose()))
    val lidar = Lidar(.15..8.0, 1800.toDegree(), 1E-3).apply {
        initialize(.0, Odometry.pose(), 0.toRad())
    }
    val lidarOnRobot = Odometry.pose(x = .15)
    val lidarToRobot = lidarOnRobot.toTransformation()

    val buffer = AtomicReference<NonOmnidirectional>(Velocity.velocity(0, 0))
    launch {
        for (command in Default.commands)
            buffer.set(Velocity.velocity(0.2 * command.v, PI / 5 * command.w))
    }
    launch {
        while (true) {
            obstacles.forEachIndexed { i, obstacle ->
                remote.paint("障碍物$i", obstacle)
            }
            delay(5000L)
        }
    }
    speedSimulation(dt = 200L) {
        buffer.get()
    }.consumeEach { (t, v) ->
        val (_, robotOnMap) = chassis.drive(v)
        val robotToMap = robotOnMap.toTransformation()
        val lidarToMap = robotToMap * lidarToRobot
        remote.paintPose("机器人", robotOnMap)
        val points =
            lidar
                .update(t * 1E-3, robotOnMap, lidarOnRobot, listOf(), obstacles)
                .map { it.data }
                .filterNot { it.distance.isNaN() }
                .map { lidarToMap(it.toVector2D()).to2D() }
                .toList()
        remote.paintFrame2("雷达", points.map { (x, y) -> x to y })
    }
}
