package org.mechdancer.simulation

import org.mechdancer.algebra.function.vector.times
import org.mechdancer.algebra.implement.vector.vector2DOf
import org.mechdancer.common.Odometry
import org.mechdancer.common.Stamped
import org.mechdancer.common.Stamped.Companion.stamp
import org.mechdancer.common.Velocity
import org.mechdancer.common.Velocity.*
import org.mechdancer.geometry.angle.toRad
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 底盘模型
 */
class Chassis {
    /** 位姿 */
    var odometry = stamp(Odometry())
        private set

    /** 运动状态 */
    var velocity: Velocity = Static
        private set

    /** 计算当前位姿 */
    operator fun get(time: Long? = null): Odometry {
        val dt = ((time ?: System.currentTimeMillis()) - odometry.time) / 1000.0
        require(dt >= 0)
        return when {
            abs(dt) < DoubleEpsilon -> odometry.data
            dt > 0                  -> {
                when (val x = velocity) {
                    is Static             -> odometry.data
                    is NonOmnidirectional -> {
                        val (v, w) = x
                        odometry.data plusDelta
                            if (abs(w) < DoubleEpsilon) {
                                Odometry(vector2DOf(v * dt, 0), .0.toRad())
                            } else {
                                val r = v / w
                                val theta = w * dt
                                Odometry(vector2DOf(sin(theta), 1 - cos(theta)) * r, theta.toRad())
                            }
                    }
                    is Omnidirectional    -> TODO()
                }
            }
            else                    -> throw IllegalArgumentException("cannot get previous pose")
        }
    }

    /** 更新速度 */
    fun drive(velocity: Velocity, time: Long? = null): Odometry {
        val now = time ?: System.currentTimeMillis()
        odometry = Stamped(now, get(now))
        this.velocity = velocity
        return odometry.data
    }

    private companion object {
        const val DoubleEpsilon = 5E-15
    }
}
