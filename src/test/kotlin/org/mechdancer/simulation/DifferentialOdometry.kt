package org.mechdancer.simulation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import org.mechdancer.algebra.function.vector.minus
import org.mechdancer.algebra.function.vector.norm
import org.mechdancer.common.Odometry
import org.mechdancer.common.Stamped
import org.mechdancer.common.filters.Differential
import org.mechdancer.common.toPose
import org.mechdancer.simulation.Default.newNonOmniRandomDriving
import org.mechdancer.simulation.Default.remote
import org.mechdancer.simulation.Default.speedSimulation
import org.mechdancer.simulation.DifferentialOdometry.Key.Left
import org.mechdancer.simulation.DifferentialOdometry.Key.Right
import org.mechdancer.struct.StructBuilderDSL.Companion.struct

// 差动里程计仿真实验
@ExperimentalCoroutinesApi
fun main() = runBlocking {
    // 起始时刻
    val t0 = 0L
    // 仿真速度
    val speed = 20
    // 机器人机械结构
    val robot = struct(Chassis(Stamped(t0, Odometry()))) {
        Encoder(Left) asSub { pose(0, +0.2) }
        Encoder(Right) asSub { pose(0, -0.2) }
    }
    // 编码器在机器人上的位姿
    val encodersOnRobot =
        robot.devices
            .mapNotNull { (device, tf) -> (device as? Encoder)?.to(tf.toPose()) }
            .toMap()
    // 里程计增量计算
    val differential = Differential(robot.what.get(), t0) { _, old, new -> new minusState old }
    // 差动里程计
    val odometry = DifferentialOdometry(0.4, Stamped(t0, Odometry()))
    // 仿真
    val random = newNonOmniRandomDriving() power speed
    speedSimulation(this, t0, 20L, speed) {
        random.next()
    }.consumeEach { (t, v) ->
        //  计算机器人位姿增量
        val current = robot.what.drive(v, t).data
        val delta = differential.update(current, t).data
        // 计算编码器增量
        for ((encoder, p) in encodersOnRobot) encoder.update(p, delta)
        // 计算里程计
        val get = { key: DifferentialOdometry.Key -> encodersOnRobot.keys.single { (k, _) -> k == key }.value }
        val pose = odometry.update(get(Left) to get(Right), t).data
        // 显示
        println("time = ${t / 1000.0}, error = ${(current.p - pose.p).norm()}")
        remote.paintPose("机器人", current)
        remote.paintPose("里程计", pose)
    }
}
