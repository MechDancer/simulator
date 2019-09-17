package org.mechdancer.simulation

import org.mechdancer.common.Velocity.Companion.velocity
import org.mechdancer.common.toPose
import org.mechdancer.common.toTransformation
import org.mechdancer.struct.StructBuilder.Companion.struct
import kotlin.math.PI

fun main() {
    val robot = struct(Chassis()) {
        sub("locator") {
            pose(0.31, 0)
            sub("encoder1") {
                pose(0.05, 0)
            }
            sub("encoder2") {
                pose(0, 0.05)
            }
            sub("encoder3") {
                pose(0, -0.05, -PI / 4)
            }
        }
    }

    while (true) {
        val robotToOdometry = robot.what.drive(velocity(0.1, 0.5)).toTransformation()
        robot
            .devices
            .mapValues { (_, toRobot) -> (robotToOdometry * toRobot).toPose() }
            .values
            .joinToString(" ") { (p, _) -> "${p.x} ${p.y} " }
            .let(::println)
        Thread.sleep(100L)
    }
}
