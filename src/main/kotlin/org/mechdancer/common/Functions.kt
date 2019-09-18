package org.mechdancer.common

import org.mechdancer.algebra.implement.vector.to2D
import org.mechdancer.algebra.implement.vector.vector2DOfZero
import org.mechdancer.geometry.angle.toAngle
import org.mechdancer.geometry.angle.toRad
import org.mechdancer.geometry.angle.toVector
import org.mechdancer.geometry.transformation.Transformation

fun Transformation.toPose(): Odometry {
    require(dim == 2) { "pose is a 2d transformation" }
    val p = invoke(vector2DOfZero()).to2D()
    val d = invokeLinear(.0.toRad().toVector()).to2D().toAngle()
    return Odometry(p, d)
}

fun Odometry.toTransformation() =
    Transformation.fromPose(p, d)

operator fun Transformation.invoke(pose: Odometry) =
    Odometry(invoke(pose.p).to2D(), invokeLinear(pose.d.toVector()).to2D().toAngle())
