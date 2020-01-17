package org.mechdancer.simulation

import org.mechdancer.algebra.doubleEquals
import org.mechdancer.algebra.function.equation.solve
import org.mechdancer.algebra.function.matrix.inverse
import org.mechdancer.algebra.function.matrix.times
import org.mechdancer.algebra.function.vector.component1
import org.mechdancer.algebra.function.vector.component2
import org.mechdancer.algebra.function.vector.component3
import org.mechdancer.algebra.implement.equation.builder.equations
import org.mechdancer.algebra.implement.matrix.builder.matrix
import org.mechdancer.algebra.implement.vector.listVectorOf
import org.mechdancer.geometry.angle.toVector
import org.mechdancer.geometry.transformation.Pose2D
import org.mechdancer.geometry.transformation.pose2D
import kotlin.math.tan

class OmniDirectionOdometry(
    encoder0: Pose2D,
    encoder1: Pose2D,
    encoder2: Pose2D
) {
    var pose = pose2D()
        private set
    private val parameters = listOf(encoder0, encoder1, encoder2)
    private val solver =
        parameters
            .map { (p, d) ->
                val (x, y) = p
                val (cos, sin) = d.toVector()
                Triple(-sin, cos, sin * x - cos * y)
            }.let { (a, b, c) ->
                val (a1, a2, a3) = a
                val (b1, b2, b3) = b
                val (c1, c2, c3) = c
                matrix {
                    row(a1, a2, a3)
                    row(b1, b2, b3)
                    row(c1, c2, c3)
                }
            }.inverse()

    fun update(v0: Double, v1: Double, v2: Double): Pose2D {
        val (u1, u2, theta) = solver * listVectorOf(v0, v1, v2)
        val (x, y) =
            if (doubleEquals(theta, .0)) {
                val (t0, t1, t2) = parameters.map { (_, d) -> d }
                val (cos0, sin0) = t0.toVector()
                val (cos1, sin1) = t1.toVector()
                val (cos2, sin2) = t2.toVector()
                equations {
                    this[cos0, sin0] = v0
                    this[cos1, sin1] = v1
                    this[cos2, sin2] = v2
                }
            } else {
                val tan = 1 / tan(theta / 2)
                equations {
                    this[1, -tan] = 2 * u1 / theta
                    this[+tan, 1] = 2 * u2 / theta
                }
            }.solve()!!
        pose = pose plusDelta pose2D(x, y, theta)
        return pose
    }
}
