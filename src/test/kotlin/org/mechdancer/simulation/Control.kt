package org.mechdancer.simulation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.mechdancer.algebra.core.Vector
import org.mechdancer.algebra.function.vector.minus
import org.mechdancer.algebra.function.vector.norm
import org.mechdancer.algebra.implement.vector.to2D
import org.mechdancer.algebra.implement.vector.vector2D
import org.mechdancer.common.Velocity.Companion.velocity
import org.mechdancer.common.Velocity.NonOmnidirectional
import org.mechdancer.common.shape.Polygon
import org.mechdancer.geometry.transformation.Pose2D
import org.mechdancer.geometry.transformation.pose2D
import org.mechdancer.geometry.transformation.toTransformation
import org.mechdancer.geometry.transformation.transform
import org.mechdancer.simulation.Default.commands
import org.mechdancer.simulation.Default.remote
import org.mechdancer.struct.StructBuilderDSL.Companion.struct
import java.util.*
import java.util.concurrent.atomic.AtomicReference

@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val buffer = AtomicReference<NonOmnidirectional>(velocity(0, 0))
    val robot = struct(Chassis())
    val chassis = Polygon(listOf(
        vector2D(+.2, +.1),
        vector2D(+.1, +.2),
        vector2D(+.1, +.25),
        vector2D(-.1, +.25),
        vector2D(-.1, +.2),
        vector2D(-.2, +.2),
        vector2D(-.4, +.1),
        vector2D(-.4, -.1),
        vector2D(-.2, -.2),
        vector2D(-.1, -.2),
        vector2D(-.1, -.25),
        vector2D(+.1, -.25),
        vector2D(+.1, -.2),
        vector2D(+.2, -.1)
    ))
    val block = Polygon(listOf(
        vector2D(-.2, +.2),
        vector2D(-.2, -.2),
        vector2D(+.2, -.2),
        vector2D(+.2, +.2)
    ))
    val path: Queue<Pose2D> = LinkedList<Pose2D>()
    launch { for (command in commands) buffer.set(velocity(0.1 * command.v, 0.5 * command.w)) }
    speedSimulation { buffer.get() }
        .consumeEach { (_, v) ->
            val (_, pose) = robot.what.drive(v)
            val odometryToRobot = pose.toTransformation().inverse()
            if (path.lastOrNull()?.takeIf { (it.p - pose.p).norm() < 0.05 } == null) {
                path.offer(pose)
                if (path.size > 40) path.poll()
            }

            remote.paint("机器人", chassis)
            remote.paintPoses("路径", path.map { odometryToRobot.transform(it) })
            val tf = odometryToRobot * pose2D(1, 1, 1).toTransformation()
            val blockOnOdometry = block.vertex.map(tf::invoke).map(Vector::to2D).let(::Polygon)
            remote.paint("障碍物", blockOnOdometry)
        }
}
