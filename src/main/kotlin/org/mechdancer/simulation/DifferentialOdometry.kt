package org.mechdancer.simulation

import org.mechdancer.algebra.function.vector.times
import org.mechdancer.algebra.implement.vector.Vector2D
import org.mechdancer.common.Stamped
import org.mechdancer.common.Stamped.Companion.stamp
import org.mechdancer.common.filters.Differential
import org.mechdancer.common.filters.Filter
import org.mechdancer.geometry.angle.toRad
import org.mechdancer.geometry.transformation.Pose2D
import org.mechdancer.geometry.transformation.plusDelta
import org.mechdancer.geometry.transformation.pose2D
import kotlin.math.cos
import kotlin.math.sin

/**
 * 差动里程计
 * @param width 轮距
 * @param origin 初始状态
 */
class DifferentialOdometry(
    private val width: Double,
    private val origin: Stamped<Pose2D>? = null
) : Filter<Pair<Double, Double>, Stamped<Pose2D>> {
    enum class Key { Left, Right }

    private val lDiff: Differential<Double, Double>
    private val rDiff: Differential<Double, Double>

    /** 位姿 */
    var odometry = origin ?: stamp(pose2D())
        private set

    init {
        lDiff = Differential(.0, odometry.time) { _, old, new -> new - old }
        rDiff = Differential(.0, odometry.time) { _, old, new -> new - old }
    }

    override fun update(new: Pair<Double, Double>, time: Long?): Stamped<Pose2D> {
        val now = time ?: System.currentTimeMillis()
        val (left, right) = new
        val l = lDiff.update(left, now).data
        val r = rDiff.update(right, now).data
        val length = (r + l) / 2
        val delta = when (val theta = (r - l) / width) {
            .0   -> pose2D(length, 0)
            else -> Pose2D(Vector2D(sin(theta), (1 - cos(theta))) * (length / theta),
                           theta.toRad())
        }
        odometry = Stamped(now, odometry.data plusDelta delta)
        return odometry
    }

    override fun clear() {
        odometry = origin ?: stamp(pose2D())
        lDiff.update(.0, odometry.time)
        rDiff.update(.0, odometry.time)
    }
}
