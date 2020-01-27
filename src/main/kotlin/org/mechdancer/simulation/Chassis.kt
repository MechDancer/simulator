package org.mechdancer.simulation

import org.mechdancer.common.Stamped
import org.mechdancer.common.Stamped.Companion.stamp
import org.mechdancer.common.Velocity
import org.mechdancer.common.Velocity.Static
import org.mechdancer.common.filters.Filter
import org.mechdancer.geometry.transformation.Pose2D
import org.mechdancer.geometry.transformation.plusDelta
import org.mechdancer.geometry.transformation.pose2D
import kotlin.math.abs

/** 通用底盘模型 */
class Chassis(origin: Stamped<Pose2D>? = null) : Filter<Velocity, Stamped<Pose2D>> {
    /** 位姿 */
    var odometry = origin ?: stamp(pose2D())
        private set

    /** 运动状态 */
    var velocity: Velocity = Static
        private set

    /** 更新速度 */
    fun drive(velocity: Velocity, time: Long? = null) = update(velocity, time)

    /** 计算当前位姿 */
    operator fun get(time: Long? = null): Pose2D {
        val (t, robotOnOdometry) = odometry
        val dt = ((time ?: System.currentTimeMillis()) - t) / 1000.0
        require(dt >= 0)
        return when {
            abs(dt) < DoubleEpsilon ->
                odometry.data
            dt > 0                  -> {
                when (val copy = velocity) {
                    is Static -> robotOnOdometry
                    else      -> robotOnOdometry plusDelta copy.toDeltaOdometry(dt)
                }
            }
            else                    ->
                throw IllegalArgumentException("cannot get previous pose")
        }
    }

    override fun update(new: Velocity, time: Long?): Stamped<Pose2D> {
        val now = time ?: System.currentTimeMillis()
        odometry = Stamped(now, get(now))
        velocity = new
        return Stamped(now, odometry.data)
    }

    override fun clear() {
        odometry = stamp(pose2D())
        velocity = Static
    }

    private companion object {
        const val DoubleEpsilon = 5E-15
    }
}
