package org.mechdancer.simulation

import org.mechdancer.common.Odometry
import org.mechdancer.common.Stamped
import org.mechdancer.common.Stamped.Companion.stamp
import org.mechdancer.common.Velocity
import org.mechdancer.common.Velocity.Static
import org.mechdancer.common.filters.Filter
import kotlin.math.abs

/** 通用底盘模型 */
class Chassis(origin: Stamped<Odometry>? = null) : Filter<Velocity, Stamped<Odometry>> {
    /** 位姿 */
    var odometry = origin ?: stamp(Odometry())
        private set

    /** 运动状态 */
    var velocity: Velocity = Static
        private set

    /** 更新速度 */
    fun drive(velocity: Velocity, time: Long? = null) = update(velocity, time)

    /** 计算当前位姿 */
    operator fun get(time: Long? = null): Odometry {
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

    override fun update(new: Velocity, time: Long?): Stamped<Odometry> {
        val now = time ?: System.currentTimeMillis()
        odometry = Stamped(now, get(now))
        velocity = new
        return Stamped(now, odometry.data)
    }

    override fun clear() {
        odometry = stamp(Odometry())
        velocity = Static
    }

    private companion object {
        const val DoubleEpsilon = 5E-15
    }
}
