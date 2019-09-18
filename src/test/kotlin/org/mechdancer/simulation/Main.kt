package org.mechdancer.simulation

import org.mechdancer.algebra.function.vector.dot
import org.mechdancer.algebra.implement.vector.Vector2D
import org.mechdancer.algebra.implement.vector.vector2DOf
import org.mechdancer.common.Odometry
import org.mechdancer.common.Velocity.Companion.velocity
import org.mechdancer.common.toPose
import org.mechdancer.geometry.angle.Angle
import org.mechdancer.geometry.angle.times
import org.mechdancer.geometry.angle.toAngle
import org.mechdancer.geometry.angle.toVector
import org.mechdancer.geometry.transformation.Transformation
import org.mechdancer.simulation.Device.Encoder
import org.mechdancer.simulation.Device.Locator
import org.mechdancer.struct.StructBuilderDSL.Companion.struct
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private enum class Coordinate {
    Odometry
}

private sealed class Device {
    object Locator : Device()
    data class Encoder(val index: Int)
}

private val robot = struct(Chassis()) {
    Locator asSub {
        pose(0.31, 0)
        Encoder(0) asSub {
            pose(0.05, 0, PI / 2)
        }
        Encoder(1) asSub {
            pose(0, 0.05, PI)
        }
        Encoder(2) asSub {
            pose(0, -0.05, -PI / 4)
        }
    }
}

private typealias Pose = Odometry

private fun move(encoder: Pose, delta: Vector2D) =
    encoder.d.toVector() dot delta

private fun rotate(encoder: Pose, delta: Angle) =
    encoder.p.length
        .takeIf { it > 0 }
        ?.let { rho ->
            delta.asRadian() * rho * sin(encoder.d.asRadian() - encoder.p.toAngle().asRadian())
        }
    ?: .0

fun main() {
    // 编码器在机器人上的位姿
    val encodersOnRobot =
        robot.devices
            .filterKeys { it is Encoder }
            .mapValues { (_, tf) -> tf.toPose() }
            .also { println(it.keys) }
    var values =
        encodersOnRobot.keys.associateWith { .0 }
    // 运动过程
    var last = robot.what.get()
    while (true) {
        val current = robot.what.drive(velocity(0.1, 0.5))
        val (dp, dd) = current minusState last
        last = current

        values = values.mapValues { (key, value) ->
            val pose = encodersOnRobot.getValue(key)
            value + 2 * rotate(pose, dd * 0.5) + move(pose, vector2DOf(dp.length, 0))
        }.also { println(it) }

        Thread.sleep(100L)
    }
}
