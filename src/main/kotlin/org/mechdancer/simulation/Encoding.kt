package org.mechdancer.simulation

import org.mechdancer.algebra.function.vector.dot
import org.mechdancer.algebra.implement.vector.Vector2D
import org.mechdancer.common.Odometry
import org.mechdancer.geometry.angle.Angle
import org.mechdancer.geometry.angle.toAngle
import org.mechdancer.geometry.angle.toVector
import kotlin.math.sin

typealias Pose = Odometry

object Encoding {
    fun move(encoder: Pose, delta: Vector2D) =
        encoder.d.toVector() dot delta

    fun rotate(encoder: Pose, delta: Angle) =
        encoder.p.length
            .takeIf { it > 0 }
            ?.let { rho ->
                delta.asRadian() * rho * sin(encoder.d.asRadian() - encoder.p.toAngle().asRadian())
            }
        ?: .0
}
