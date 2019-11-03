package org.mechdancer.simulation

import org.mechdancer.algebra.function.vector.euclid
import org.mechdancer.algebra.function.vector.times
import org.mechdancer.algebra.implement.vector.to2D
import org.mechdancer.common.*
import org.mechdancer.common.Odometry.Companion.pose
import org.mechdancer.common.Velocity.Companion.velocity
import org.mechdancer.geometry.angle.Angle
import org.mechdancer.geometry.angle.rotate
import org.mechdancer.geometry.angle.toRad
import org.mechdancer.geometry.angle.toVector
import org.mechdancer.simulation.map.shape.Polygon
import org.mechdancer.simulation.map.shape.rangeTo

class Lidar(
    validRange: ClosedFloatingPointRange<Double>,
    speed: Angle,
    private val interval: Double
) {
    // 转速（弧度 / 秒）
    private val radPerSecond = speed.asRadian()
    private val rotateStep = (radPerSecond * interval).toRad()
    // 盲区
    private val minDistance = validRange.start
    // 视野
    private val maxDistance = validRange.endInclusive

    // 状态
    private var lastT = .0
    private var lastPose = pose()
    private var lastAngle = 0.toRad()

    /** 初始化 */
    fun initialize(t: Double, pose: Odometry, angle: Angle) {
        lastT = t
        lastPose = pose
        lastAngle = angle
    }

    fun update(
        time: Double,
        robotOnMap: Odometry,
        lidarOnRobot: Odometry,
        obstacles: List<Polygon>
    ): Sequence<Stamped<Polar>> {
        // 初始化
        var t = lastT
        var angle = lastAngle
        var pose = lastPose
        // 检测
        require(t < time)
        // 计算步骤增量
        val dt = time - t
        val (dp, dd) = robotOnMap minusState pose
        val step =
            velocity(dp.x / dt, dp.y / dt, dd.asRadian() / dt)
                .toDeltaOdometry(interval)
        // 修改状态
        lastT = time
        lastAngle = lastAngle rotate (radPerSecond * dt).toRad()
        lastPose = robotOnMap
        // 循环体
        return sequence {
            while (t < time) {
                // 计算变换关系
                val robotToMap = pose.toTransformation()
                val lidarOnMap = robotToMap(lidarOnRobot)
                val lidarToMap = lidarOnMap.toTransformation()
                // 计算碰撞
                val direction = angle.toVector()
                val lightOnMap = lidarOnMap.p..lidarToMap(direction * maxDistance).to2D()
                var min = Double.MAX_VALUE
                val blind =
                    obstacles
                        .asSequence()
                        .flatMap { it.intersect(lightOnMap).asSequence() }
                        .map { it euclid lidarOnMap.p }
                        .any {
                            if (min > it) min = it
                            it < minDistance
                        }
                val distance = if (!blind && min < maxDistance) min else Double.NaN
                // 发送
                yield(Stamped((t * 1E-3).toLong(), Polar(distance, angle.asRadian())))
                // 更新状态
                t += interval
                angle = angle rotate rotateStep
                pose = pose plusDelta step
            }
        }
    }
}
