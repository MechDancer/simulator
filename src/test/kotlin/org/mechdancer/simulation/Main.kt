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
