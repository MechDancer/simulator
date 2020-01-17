package org.mechdancer.simulation

import org.mechdancer.algebra.function.vector.div
import org.mechdancer.algebra.function.vector.dot
import org.mechdancer.algebra.function.vector.minus
import org.mechdancer.algebra.function.vector.plus
import org.mechdancer.algebra.implement.vector.Vector2D
import org.mechdancer.algebra.implement.vector.vector2DOf
import org.mechdancer.geometry.angle.toVector
import org.mechdancer.geometry.transformation.Pose2D
import kotlin.math.tan

/** 模拟编码器 */
class Encoder(val key: Any) {
    var value = .0
        private set

    operator fun component1() = key
    operator fun component2() = value

    override fun equals(other: Any?) =
        other is Encoder && other.key == key

    override fun hashCode() =
        key.hashCode()

    override fun toString() = "encoder<$key> = $value"

    fun update(encoderOnRobot: Pose2D, robotDelta: Pose2D): Double {
        val (p, d) = robotDelta
        value += when (val theta = d.asRadian()) {
            .0   -> encoderOnRobot.d.toVector() dot p
            else -> {
                val (ep, ed) = encoderOnRobot
                ((p / 2).let { it + it.rotate90() / tan(theta / 2) - ep } dot ed.toVector().rotate90()) * theta
            }
        }
        return value
    }

    private companion object {
        fun Vector2D.rotate90() = vector2DOf(-y, +x)
    }
}
