package org.mechdancer.simulation

import org.mechdancer.algebra.implement.vector.vector2DOf
import org.mechdancer.common.Velocity.Companion.velocity
import org.mechdancer.common.filters.DiscreteDelayer.Companion.delayOn
import org.mechdancer.common.toPose
import org.mechdancer.geometry.angle.times
import org.mechdancer.simulation.Device.Encoder
import org.mechdancer.simulation.Device.Locator
import org.mechdancer.simulation.Encoding.move
import org.mechdancer.simulation.Encoding.rotate
import org.mechdancer.struct.StructBuilderDSL.Companion.struct
import kotlin.math.PI

private sealed class Device {
    object Locator : Device()
    data class Encoder(val index: Int)
}

private val robot = struct(Chassis()) {
    Locator asSub {
        pose(0.31, 0)
        Encoder(0) asSub {
            pose(0, 0.05, PI)
        }
        Encoder(1) asSub {
            pose(0.05, 0, PI / 2)
        }
        Encoder(2) asSub {
            pose(0, -0.05, -PI / 4)
        }
    }
}

fun main() {
    // 编码器在机器人上的位姿
    val encodersOnRobot =
        robot.devices
            .filterKeys { it is Encoder }
            .mapValues { (_, tf) -> tf.toPose() }
            .also { println(it.keys) }
    // 编码器读数
    var values =
        encodersOnRobot.keys.associateWith { .0 }
    // 运动过程
    val queue = delayOn(robot.what.get())
    while (true) {
        //  计算机器人位姿增量
        val current = robot.what.drive(velocity(0.1, 0)).data
        val last = queue.update(current)!!.data
        val (dp, dd) = current minusState last
        // 计算编码器增量
        values = values.mapValues { (key, value) ->
            val pose = encodersOnRobot.getValue(key)
            value + 2 * rotate(pose, dd * 0.5) + move(pose, vector2DOf(dp.length, 0))
        }

        println(values)

        Thread.sleep(100L)
    }
}
