package org.mechdancer.simulation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.mechdancer.common.filters.Differential
import org.mechdancer.common.toPose
import org.mechdancer.simulation.Default.newOmniRandomDriving
import org.mechdancer.simulation.Default.remote
import org.mechdancer.struct.StructBuilderDSL.Companion.struct
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

// 机器人机械结构
private val robot = struct(Chassis()) {
    "定位模块" asSub {
        pose(0.31, 0)
        Encoder(0) asSub {
            pose(0, 0.05, PI)
        }
        Encoder(1) asSub {
            pose(0.05, 0, PI / 2)
        }
        Encoder(2) asSub {
            pose(0, -0.05, PI / 4)
        }
    }
}

// 编码器在机器人上的位姿
private val encodersOnRobot =
    robot.devices
        .mapNotNull { (device, tf) -> (device as? Encoder)?.to(tf.toPose()) }
        .toMap()

@ExperimentalCoroutinesApi
fun main() = runBlocking {
    // 离散差分环节
    val differential = Differential(robot.what.get()) { _, old, new -> new minusState old }
    produce {
        newOmniRandomDriving().run {
            while (true) {
                send(next())
                delay(100L)
            }
        }
    }.consumeEach { v ->
        //  计算机器人位姿增量
        robot.what.drive(v)
            .data
            .also { pose -> remote.paint("pose", pose.p.x, pose.p.y, pose.d.asRadian()) }
            .let { differential.update(it) }
            .data
            .let { delta ->
                // 计算编码器增量
                encodersOnRobot
                    .onEach { (encoder, pose) -> encoder.update(pose, delta) }
            }
            .keys
            .also(::println)
    }
}


fun x(e0y: Double, e90x: Double, v0: Double, v90: Double, v45: Double) =
    (2.0 * (2 * e90x.pow(2) + e0y * (-2 * e0y * (v90 - sqrt(2.0) * v45)) + v90 * (v0 + v90) - sqrt(2.0) * v45) +
            e90x * (v0 * (v0 + v90) - sqrt(2.0) * (2 * v0 + v90) * v45 + 2 * v45.pow(2) - e0y * (v0 - v90 + sqrt(2.0) * v45))) /
            (4 * (e90x - e0y).pow(2) + v0.pow(2) + v90.pow(2) - 2 * sqrt(2.0) * v90 * v45 + 2 * v45.pow(2))

fun y(e0y: Double, e90x: Double, v0: Double, v90: Double, v45: Double) =
    (2 * (e90x * (-2 * e0y + v0) * v90 - 2 * e90x.pow(2) * (v0 - sqrt(2.0) * v45) + e90x * (2 * e0y + v0) * (v0 - sqrt(
        2.0
    ) * v45) +
            e0y * (v90 * (2 * e0y + v0 + v90) - sqrt(2.0) * (v0 + 2 * v90) * v45 + 2 * v45.pow(2)))) /
            (4 * (e90x - e0y).pow(2) + v0.pow(2) + v90.pow(2) - 2 * sqrt(2.0) * v90 * v45 + 2 * v45.pow(2) + 2 * v0 * (v90 - sqrt(
                2.0
            ) * v45))

fun theta(e0y: Double, e90x: Double, v0: Double, v90: Double, v45: Double) = (v0 + v90 - sqrt(2.0) * v45) / (e90x - e0y)
