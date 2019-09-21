package org.mechdancer.simulation

import org.mechdancer.algebra.function.vector.div
import org.mechdancer.algebra.function.vector.dot
import org.mechdancer.algebra.function.vector.plus
import org.mechdancer.algebra.function.vector.times
import org.mechdancer.algebra.implement.vector.to2D
import org.mechdancer.common.Odometry
import org.mechdancer.common.toTransformation
import org.mechdancer.geometry.angle.rotate
import org.mechdancer.geometry.angle.toAngle
import org.mechdancer.geometry.angle.toRad
import org.mechdancer.geometry.angle.toVector
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class Encoder(val key: Any) {
    var value = .0
        private set

    override fun equals(other: Any?) =
        other is Encoder && other.key == key

    override fun hashCode() =
        key.hashCode()

    override fun toString() = "encoder<$key> = $value"

    fun update(encoderOnRobot: Odometry, robotDelta: Odometry): Double {
        val (p, d) = robotDelta
        value += when (val theta = d.asRadian()) {
            .0   -> encoderOnRobot.d.toVector() dot p
            else -> {
                val halfP = p / 2
                // 机器人运动的圆弧半径
                val r = halfP.length / sin(theta / 2)
                val centerOnRobot = halfP + (p.toAngle() rotate positive90).toVector() * r * cos(theta)
                val centerOnEncoder = (-encoderOnRobot.toTransformation())(centerOnRobot).to2D()
                val k = cos((centerOnEncoder.toAngle() rotate negative90).asRadian())
                k * centerOnEncoder.length * theta
            }
        }
        return value
    }

    private companion object {
        val positive90 = (+PI / 2).toRad()
        val negative90 = (-PI / 2).toRad()
    }
}
