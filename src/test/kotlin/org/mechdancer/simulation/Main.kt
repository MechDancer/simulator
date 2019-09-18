package org.mechdancer.simulation

import org.mechdancer.algebra.function.vector.div
import org.mechdancer.algebra.function.vector.dot
import org.mechdancer.algebra.function.vector.plus
import org.mechdancer.algebra.function.vector.times
import org.mechdancer.algebra.implement.vector.to2D
import org.mechdancer.common.Odometry
import org.mechdancer.common.Velocity.Companion.velocity
import org.mechdancer.common.filters.DiscreteDelayer.Companion.delayOn
import org.mechdancer.common.toPose
import org.mechdancer.common.toTransformation
import org.mechdancer.geometry.angle.rotate
import org.mechdancer.geometry.angle.toAngle
import org.mechdancer.geometry.angle.toRad
import org.mechdancer.geometry.angle.toVector
import org.mechdancer.simulation.Device.Encoder
import org.mechdancer.simulation.Device.Locator
import org.mechdancer.struct.StructBuilderDSL.Companion.struct
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
        val current = robot.what.drive(velocity(0.1, 0.5)).data
        val last = queue.update(current)!!.data
        val (dp, dd) = current minusState last  // `this robot` on `last robot`
        // 确定计算方法
        val calculate = when (val theta = dd.asRadian()) {
            .0   -> { encoder: Odometry ->
                encoder.d.toVector() dot dp
            }
            else -> { encoder: Odometry ->
                // 机器人运动的圆弧半径
                val r = dp.length / sin(theta) / 2
                val centerOnLast = (dp / 2).let {
                    it + (it.toAngle() rotate (PI / 2).toRad()).toVector() * r * cos(theta)
                }
                val centerOnEncoder = (-encoder.toTransformation())(centerOnLast).to2D()
                val k = cos((centerOnEncoder.toAngle() rotate (-PI / 2).toRad()).asRadian())
                k * centerOnEncoder.length * theta
            }
        }
        // 计算编码器增量
        values = values.mapValues { (encoder, value) ->
            value + calculate(encodersOnRobot.getValue(encoder))
        }
        println(values)
        Thread.sleep(100L)
    }
}
