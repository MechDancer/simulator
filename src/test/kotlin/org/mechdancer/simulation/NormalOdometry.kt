package org.mechdancer.simulation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import org.mechdancer.algebra.function.vector.minus
import org.mechdancer.algebra.function.vector.norm
import org.mechdancer.common.Stamped
import org.mechdancer.common.filters.Differential
import org.mechdancer.geometry.transformation.minusState
import org.mechdancer.geometry.transformation.pose2D
import org.mechdancer.geometry.transformation.toPose2D
import org.mechdancer.simulation.Default.newOmniRandomDriving
import org.mechdancer.simulation.Default.remote
import org.mechdancer.struct.StructBuilderDSL.Companion.struct
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.PI

// 机器人机械结构
private val robot = struct(Chassis(Stamped(0L, pose2D()))) {
    Encoder(0) asSub { where(0, +.2, 0) }
    Encoder(1) asSub { where(0, -.2, 0) }
    Encoder(2) asSub { where(0, 0, PI / 2) }
}

// 编码器在机器人上的位姿
private val encodersOnRobot =
    robot.devices
        .mapNotNull { (device, tf) -> (device as? Encoder)?.to(tf.toPose2D()) }
        .toMap()

@ExperimentalCoroutinesApi
fun main() = runBlocking {
    // 位姿差分
    val differential = Differential(robot.what.get()) { _, old, new -> new minusState old }
    // 编码器值差分
    val dValue = List(3) { Differential(.0) { _, old, new -> new - old } }
    // 里程计
    val odometry = encodersOnRobot.toList().run {
        OmniDirectionOdometry(single { (e, _) -> e.key == 0 }.second,
                              single { (e, _) -> e.key == 1 }.second,
                              single { (e, _) -> e.key == 2 }.second)
    }
    val random = newOmniRandomDriving()
    speedSimulation { random.next() }
        .consumeEach { (t, v) ->
            //  计算机器人位姿增量
            val actual = robot.what.drive(v, t).data
            val delta = differential.update(actual).data
            // 更新编码器值
            for ((encoder, parameter) in encodersOnRobot)
                encoder.update(parameter, delta)
            // 计算里程计值
            val pose = encodersOnRobot.keys
                .let { set -> (0..2).map { i -> dValue[i].update(set.single { it.key == i }.value).data } }
                .let { (v0, v1, v2) -> odometry.update(v0, v1, v2) }
            // 显示
            remote.paint("机器人", actual)
            remote.paint("里程计", pose)
            println("actual = $actual, pose = $pose, error = ${(actual.p - pose.p).norm()}")
        }
}
