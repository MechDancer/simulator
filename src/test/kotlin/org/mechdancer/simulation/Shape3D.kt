package org.mechdancer.simulation

import org.mechdancer.algebra.function.vector.normalize
import org.mechdancer.algebra.function.vector.times
import org.mechdancer.algebra.implement.vector.vector3D
import org.mechdancer.algebra.implement.vector.vector3DOfZero
import org.mechdancer.geometry.transformation.Pose3D
import org.mechdancer.geometry.transformation.pose3D
import org.mechdancer.simulation.Default.remote
import kotlin.math.PI
import kotlin.random.Random

val random get() = Random.nextDouble(-1.0, 1.0)

fun main() {
    val splitter = vector3D(Double.NaN, Double.NaN, Double.NaN)
    val shape = listOf(
        vector3D(+1, +1, +1),
        vector3D(-1, +1, +1),
        vector3D(-1, -1, +1),
        vector3D(+1, -1, +1),
        vector3D(+1, -1, -1),
        vector3D(-1, -1, -1),
        vector3D(-1, +1, -1),
        vector3D(+1, +1, -1),
        splitter,
        vector3D(+1, -1, -1),
        vector3D(+1, +1, -1),
        vector3D(+1, +1, +1),
        vector3D(+1, -1, +1),
        splitter,
        vector3D(-1, +1, +1),
        vector3D(-1, +1, -1),
        splitter,
        vector3D(-1, -1, +1),
        vector3D(-1, -1, -1))
    var dir = pose3D()
    while (true) {
        dir *= Pose3D(vector3DOfZero(), vector3D(1, 2, 3).normalize() * (PI / 1000))
        remote.paint3DFrame("立方体不动", shape)
        remote.paint3DFrame("立方体旋转", shape.map { if (it == splitter) splitter else dir * it })
        remote.paint("随机三维", vector3D(random, random, random))
        Thread.sleep(1000 / 30)
    }
}
