package org.mechdancer.simulation

import org.mechdancer.algebra.function.vector.times
import org.mechdancer.algebra.implement.vector.vector2DOf
import org.mechdancer.common.Odometry
import org.mechdancer.common.Stamped
import org.mechdancer.common.Stamped.Companion.stamp
import org.mechdancer.common.filters.Differential
import org.mechdancer.common.filters.Filter
import org.mechdancer.geometry.angle.toRad
import kotlin.math.cos
import kotlin.math.sin

/**
 * 差动里程计
 * @param width 轮距
 * @param origin 初始状态
 * @param error 测量误差函数
 */
class DifferentialOdometry(
    private val width: Double,
    private val origin: Stamped<Odometry>? = null,
    private val error: (dl: Double, dr: Double) -> Pair<Double, Double> = { dl, dr -> dl to dr }
) : Filter<Pair<Double, Double>, Stamped<Odometry>> {
    enum class Key { Left, Right }

    private val lDiff: Differential<Double, Double>
    private val rDiff: Differential<Double, Double>

    /** 位姿 */
    var odometry = origin ?: stamp(Odometry())
        private set

    init {
        lDiff = Differential(.0, odometry.time) { _, old, new -> new - old }
        rDiff = Differential(.0, odometry.time) { _, old, new -> new - old }
    }

    override fun update(new: Pair<Double, Double>, time: Long?): Stamped<Odometry> {
        val now = time ?: System.currentTimeMillis()
        val (left, right) = new
        val dl = lDiff.update(left, now).data
        val dr = rDiff.update(right, now).data
        val (l, r) = error(dl, dr)
        val length = (r + l) / 2
        val delta = when (val theta = (r - l) / width) {
            .0   -> Odometry(vector2DOf(length, 0))
            else -> Odometry(vector2DOf(sin(theta), (1 - cos(theta))) * (length / theta),
                             theta.toRad())
        }
        odometry = Stamped(now, odometry.data plusDelta delta)
        return odometry
    }

    override fun clear() {
        odometry = origin ?: stamp(Odometry())
        lDiff.update(.0, odometry.time)
        rDiff.update(.0, odometry.time)
    }
}
