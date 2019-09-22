package org.mechdancer.simulation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
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
import org.mechdancer.simulation.Default.speedSimulation
import org.mechdancer.simulation.DifferentialOdometry.Key.Left
import org.mechdancer.simulation.DifferentialOdometry.Key.Right
import org.mechdancer.struct.StructBuilderDSL.Companion.struct
import kotlin.math.cos
import kotlin.math.sin

private class DifferentialOdometry(
    private val width: Double,
    t0: Long? = null,
    private val error: (dl: Double, dr: Double) -> Pair<Double, Double> = { dl, dr -> dl to dr }
) {
    enum class Key { Left, Right }

    private val lDiff: Differential<Double, Double>
    private val rDiff: Differential<Double, Double>

    init {
        val now = t0 ?: System.currentTimeMillis()
        lDiff = Differential(.0, now) { _, old, new -> new - old }
        rDiff = Differential(.0, now) { _, old, new -> new - old }
    }

    /** 通过 [time] 时刻的左右两轮编码器值计算里程计增量 */
    fun calculate(left: Double, right: Double, time: Long? = null): Odometry {
        val now = time ?: System.currentTimeMillis()
        val dl = lDiff.update(left, now).data
        val dr = rDiff.update(right, now).data
        val (l, r) = error(dl, dr)
        val length = (r + l) / 2
        return when (val theta = (r - l) / width) {
            .0   -> Odometry(vector2DOf(length, 0))
            else -> Odometry(vector2DOf(sin(theta), (1 - cos(theta))) * (length / theta),
                             theta.toRad())
        }
    }
}

// 差动里程计仿真实验
@ExperimentalCoroutinesApi
fun main() = runBlocking {
    val t0 = 0L
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
    // 离散差分环节
    val differential = Differential(robot.what.get(), t0) { _, old, new -> new minusState old }
    // 里程计缓存
    var pose = Odometry()
    val odometry = DifferentialOdometry(0.4, t0)
    // 仿真
    val random = newNonOmniRandomDriving()
    speedSimulation(this, t0, 10L) {
        random.next()
    }.consumeEach { (t, v) ->
        //  计算机器人位姿增量
        val current = robot.what.drive(v, t).data
        val delta = differential.update(current, t).data
        // 计算编码器增量
        for ((encoder, p) in encodersOnRobot) encoder.update(p, delta)
        // 计算里程计
        val get = { key: DifferentialOdometry.Key -> encodersOnRobot.keys.single { (k, _) -> k == key }.value }
        pose = pose plusDelta odometry.calculate(get(Left), get(Right), t)
        // 显示
        println("time = ${t / 1000.0}, error = ${(current.p - pose.p).norm()}")
        remote.paintPose("机器人", current)
        remote.paintPose("里程计", pose)
    }
}
