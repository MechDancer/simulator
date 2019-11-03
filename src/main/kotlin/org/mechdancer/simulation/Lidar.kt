package org.mechdancer.simulation

import org.mechdancer.algebra.function.vector.norm
import org.mechdancer.algebra.function.vector.times
import org.mechdancer.common.Odometry
import org.mechdancer.common.Polar
import org.mechdancer.common.toTransformation
import org.mechdancer.geometry.angle.*
import org.mechdancer.simulation.map.shape.Polygon
import org.mechdancer.simulation.map.shape.rangeTo
import kotlin.math.PI
import kotlin.math.roundToInt

class Lidar(val key: Any, resolution: Angle) {
    var direction: Angle = 0.toRad()
        private set
    private val pointCount = (2 * PI / resolution.asRadian()).roundToInt()
    operator fun get(pose: Odometry, obstacles: List<Polygon>) {
        val tf = pose.toTransformation()
        sequence {
            for (i in 0 until 360) {
                val d = i.toDegree().toVector()
                yield(d * .15..d * 10.0)
            }
        }.mapNotNull { light ->
            obstacles
                .asSequence()
                .flatMap { it.intersect(light).asSequence() }
                .map { Polar(it.norm(), it.toAngle().asRadian()) }
                .minBy { it.distance }
        }.toList()
    }
}
