package org.mechdancer.common

import org.mechdancer.algebra.function.vector.times
import org.mechdancer.algebra.implement.vector.vector2DOf
import org.mechdancer.geometry.angle.rotate
import org.mechdancer.geometry.angle.toAngle
import org.mechdancer.geometry.angle.toRad
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** 标准底盘运动学模型下的控制量 */
sealed class Velocity {
    abstract fun toDeltaOdometry(dt: Double): Odometry

    /** 静止不动 */
    object Static : Velocity() {
        override fun toDeltaOdometry(dt: Double) = Odometry()
    }

    /** 非全向 */
    data class NonOmnidirectional(
        val v: Double,
        val w: Double
    ) : Velocity() {
        override fun toDeltaOdometry(dt: Double) =
            Omnidirectional(v, .0, w).toDeltaOdometry(dt)
    }

    /** 全向 */
    data class Omnidirectional(
        val vx: Double,
        val vy: Double,
        val w: Double
    ) : Velocity() {
        override fun toDeltaOdometry(dt: Double) =
            if (abs(w) < DoubleEpsilon) {
                Odometry(vector2DOf(vx * dt, vy * dt), .0.toRad())
            } else {
                val v = vector2DOf(vx, vy)
                val r = v.length / w
                val theta = w * dt
                Odometry(vector2DOf(sin(theta), 1 - cos(theta)) * r rotate v.toAngle(),
                         theta.toRad())
            }
    }

    companion object {
        const val DoubleEpsilon = 5E-15

        fun velocity() = Static

        fun velocity(v: Number, w: Number) =
            NonOmnidirectional(v.toDouble(), w.toDouble())

        fun velocity(vx: Number, vy: Number, w: Number) =
            Omnidirectional(vx.toDouble(), vy.toDouble(), w.toDouble())
    }
}
