package org.mechdancer.common.shape

import org.mechdancer.algebra.function.vector.norm
import org.mechdancer.algebra.function.vector.times
import org.mechdancer.algebra.implement.vector.vector2D
import org.mechdancer.common.Polar
import org.mechdancer.geometry.angle.toAngle
import org.mechdancer.geometry.angle.toDegree
import org.mechdancer.geometry.angle.toVector
import org.mechdancer.simulation.Default
import org.mechdancer.simulation.paint
import org.mechdancer.simulation.paintVectors
import org.mechdancer.simulation.random.Normal
import kotlin.concurrent.thread

fun main() {
    val obstacles = listOf(
        Polygon(listOf(
            vector2D(1, 1),
            vector2D(5, 1),
            vector2D(5, 5),
            vector2D(1, 5))),
        Polygon(listOf(
            vector2D(1, -2),
            vector2D(3, -1),
            vector2D(2, -6))),
        Polygon(listOf(
            vector2D(-3, +10),
            vector2D(-4, +10),
            vector2D(-4, -10),
            vector2D(-3, -10))))
    val points =
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
    val inner = Circle(.15, 64).sample()
    val outer = Circle(10.0, 64).sample()
    thread {
        while (true) {
            obstacles.forEachIndexed { i, item ->
                Default.remote.paint("物体$i", item)
            }
            Default.remote.paint("15cm", inner)
            Default.remote.paint("10m", outer)
            Default.remote.paint("机器人", .0, .0, .0)
            Thread.sleep(1000L)
        }
    }
    while (true) {
        Default.remote.paintVectors(
            "激光雷达",
            points.map { it.copy(distance = it.distance * Normal.next(1.0, .01)).toVector2D() })
        Thread.sleep(100L)
    }
}
