package org.mechdancer.simulation

import org.mechdancer.algebra.function.vector.euclid
import org.mechdancer.algebra.function.vector.times
import org.mechdancer.common.Polar
import org.mechdancer.common.Stamped
import org.mechdancer.common.Velocity.Companion.velocity
import org.mechdancer.common.shape.Polygon
import org.mechdancer.common.shape.rangeTo
import org.mechdancer.geometry.angle.Angle
import org.mechdancer.geometry.angle.rotate
import org.mechdancer.geometry.angle.toRad
import org.mechdancer.geometry.angle.toVector
import org.mechdancer.geometry.transformation.Pose2D
import org.mechdancer.geometry.transformation.minusState
import org.mechdancer.geometry.transformation.plusDelta
import org.mechdancer.geometry.transformation.pose2D
import kotlin.math.min

/**
 * 激光雷达
 * @param validRange 有效距离范围
 * @param speed 转速
 * @param interval 采样周期（秒）
 */
class Lidar(
    validRange: ClosedFloatingPointRange<Double>,
    speed: Angle,
    private val interval: Double
) {
    // 转速（弧度 / 秒）
    private val radPerSecond = speed.rad
    private val rotateStep = (radPerSecond * interval).toRad()

    // 盲区
    private val minDistance = validRange.start

    // 视野
    private val maxDistance = validRange.endInclusive

    // 状态
    private var lastT = .0
    private var lastPose = pose2D()
    private var lastAngle = 0.toRad()

    /** 初始化 */
    fun initialize(t: Double, pose: Pose2D, angle: Angle) {
        lastT = t
        lastPose = pose
        lastAngle = angle
    }

    /**
     * 更新
     *
     * @param time 时刻（秒）
     * @param robotOnMap 当前机器人位姿
     * @param lidarOnRobot 雷达安装位置
     * @param cover 机器人上的遮挡
     * @param obstacles 地图上的障碍物
     */
    fun update(
        time: Double,
        robotOnMap: Pose2D,
        lidarOnRobot: Pose2D,
        cover: List<Polygon>,
        obstacles: List<Polygon>
    ): Sequence<Stamped<Polar>> {
        // 初始化
        var t = lastT
        var angle = lastAngle
        var pose = lastPose
        // 检测
        val dt = time - t
        require(dt > 0)
        // 修改状态
        lastT = time
        lastAngle = lastAngle rotate (radPerSecond * dt).toRad()
        lastPose = robotOnMap
        return sequence {
            // 计算步骤增量
            val (dp, dd) = robotOnMap minusState pose
            val step =
                velocity(
                    dp.x / dt,
                    dp.y / dt,
                    dd.rad / dt
                ).toDeltaOdometry(interval)
            // 循环体
            while (t < time) {
                // 计算
                val lidarOnMap = pose * lidarOnRobot
                val min =
                    cover.intersect(lidarOnRobot, angle)
                        ?.let { a ->
                            obstacles.intersect(lidarOnMap, angle)
                                ?.let { b -> min(a, b) }
                        }
                        ?.takeIf { it < Double.MAX_VALUE }
                    ?: Double.NaN
                // 发送
                yield(Stamped((t * 1E-3).toLong(), Polar(min, angle)))
                // 更新状态
                t += interval
                angle = angle rotate rotateStep
                pose = pose plusDelta step
            }
        }
    }

    // 计算碰撞
    private fun List<Polygon>.intersect(
        lidarOnSystem: Pose2D,
        angle: Angle
    ): Double? {
        val endOnLidar = angle.toVector() * maxDistance
        val light = lidarOnSystem.p..lidarOnSystem * endOnLidar
        var min = Double.MAX_VALUE
        val blind =
            asSequence()
                .flatMap { it.intersect(light).asSequence() }
                .map { it euclid light.begin }
                .any {
                    if (min > it) min = it
                    it < minDistance
                }
        return min.takeIf { !blind }
    }
}
