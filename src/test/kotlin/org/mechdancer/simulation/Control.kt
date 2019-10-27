package org.mechdancer.simulation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.mechdancer.algebra.function.vector.minus
import org.mechdancer.algebra.function.vector.norm
import org.mechdancer.algebra.implement.vector.Vector2D
import org.mechdancer.algebra.implement.vector.vector2DOf
import org.mechdancer.common.Odometry
import org.mechdancer.common.Odometry.Companion.odometry
import org.mechdancer.common.Stamped
import org.mechdancer.common.Velocity.Companion.velocity
import org.mechdancer.common.Velocity.NonOmnidirectional
import org.mechdancer.common.invoke
import org.mechdancer.common.toTransformation
import org.mechdancer.simulation.Default.commands
import org.mechdancer.simulation.Default.remote
import org.mechdancer.struct.StructBuilderDSL.Companion.struct
import java.util.*
import java.util.concurrent.atomic.AtomicReference

private fun shape(vararg vertex: Vector2D) =
    vertex.toList().let { it + it.first() }

private fun Iterable<Vector2D>.put(pose: Odometry) =
    map(pose.toTransformation()::invoke)

@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val buffer = AtomicReference<NonOmnidirectional>(velocity(0, 0))
    val robot = struct(Chassis(Stamped(0, Odometry())))
    val chassis = shape(vector2DOf(+.2, +.1),
                        vector2DOf(+.1, +.2),
                        vector2DOf(+.1, +.25),
                        vector2DOf(-.1, +.25),
                        vector2DOf(-.1, +.2),
                        vector2DOf(-.2, +.2),
                        vector2DOf(-.4, +.1),
                        vector2DOf(-.4, -.1),
                        vector2DOf(-.2, -.2),
                        vector2DOf(-.1, -.2),
                        vector2DOf(-.1, -.25),
                        vector2DOf(+.1, -.25),
                        vector2DOf(+.1, -.2),
                        vector2DOf(+.2, -.1))
    val block = shape(vector2DOf(-.2, +.2),
                      vector2DOf(-.2, -.2),
                      vector2DOf(+.2, -.2),
                      vector2DOf(+.2, +.2)).put(odometry(1, 1, 0))
    val path: Queue<Odometry> = LinkedList<Odometry>()
    launch { for (command in commands) buffer.set(velocity(0.1 * command.v, 0.5 * command.w)) }
    speedSimulation { buffer.get() }
        .consumeEach { (_, v) ->
            val (_, pose) = robot.what.drive(v)
            val odometryToRobot = pose.toTransformation().inverse()
            if (path.lastOrNull()?.takeIf { (it.p - pose.p).norm() < 0.05 } == null) {
                path.offer(pose)
                if (path.size > 40) path.poll()
            }

            remote.paintVectors("机器人", chassis)
            remote.paintVectors("障碍物", block.map(odometryToRobot::invoke))
            remote.paintPoses("路径", path.map { odometryToRobot.invoke(it) })
        }
}
