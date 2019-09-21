package org.mechdancer.simulation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.mechdancer.algebra.function.vector.minus
import org.mechdancer.algebra.function.vector.norm
import org.mechdancer.algebra.function.vector.times
import org.mechdancer.algebra.implement.vector.vector2DOf
import org.mechdancer.common.Odometry
import org.mechdancer.common.Stamped
import org.mechdancer.common.filters.Differential
import org.mechdancer.common.toPose
import org.mechdancer.geometry.angle.toRad
import org.mechdancer.simulation.Default.newNonOmniRandomDriving
import org.mechdancer.simulation.Default.remote
import org.mechdancer.struct.StructBuilderDSL.Companion.struct
import kotlin.math.cos
import kotlin.math.sin

// 机器人机械结构
private val robot = struct(Chassis(Stamped(0, Odometry()))) {
    Encoder("left") asSub { pose(0, +0.2) }
    Encoder("right") asSub { pose(0, -0.2) }
}

// 编码器在机器人上的位姿
private val encodersOnRobot =
    robot.devices
        .mapNotNull { (device, tf) -> (device as? Encoder)?.to(tf.toPose()) }
        .toMap()

@ExperimentalCoroutinesApi
fun main() = runBlocking {
    var time = 0L
    // 离散差分环节
    val differential = Differential(robot.what.get(), time) { _, old, new -> new minusState old }
    // 里程计缓存
    var odometry = Odometry()
    val lDiff = Differential(.0, time) { _, old, new -> new - old }
    val rDiff = Differential(.0, time) { _, old, new -> new - old }
    produce {
        newNonOmniRandomDriving().run {
            while (true) {
                send(next())
                delay(1L)
            }
        }
    }.consumeEach { v ->
        time += 100
        //  计算机器人位姿增量
        val current = robot.what.drive(v, time).data
        val delta = differential.update(current, time).data
        // 计算编码器增量
        for ((encoder, pose) in encodersOnRobot) encoder.update(pose, delta)
        // 计算里程计
        val l = lDiff.update(encodersOnRobot.keys.single { it.key == "left" }.value, time).data
        val r = rDiff.update(encodersOnRobot.keys.single { it.key == "right" }.value, time).data
        val length = (r + l) / 2
        odometry = odometry plusDelta when (val theta = (r - l) / 0.4) {
            .0   -> Odometry(vector2DOf(length, 0))
            else -> Odometry(vector2DOf(sin(theta), (1 - cos(theta))) * (length / theta),
                             theta.toRad())
        }
        // 显示
        println("time = ${time / 1000.0}, error = ${(current.p - odometry.p).norm()}")
        remote.paint("pose", current.p.x, current.p.y, current.d.asRadian())
        remote.paint("odometry", odometry.p.x, odometry.p.y, odometry.d.asRadian())
    }
}
