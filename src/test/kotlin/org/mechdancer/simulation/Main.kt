package org.mechdancer.simulation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import org.mechdancer.algebra.doubleEquals
import org.mechdancer.algebra.function.equation.solve
import org.mechdancer.algebra.function.matrix.inverse
import org.mechdancer.algebra.function.matrix.times
import org.mechdancer.algebra.function.vector.*
import org.mechdancer.algebra.implement.equation.builder.equations
import org.mechdancer.algebra.implement.matrix.builder.matrix
import org.mechdancer.algebra.implement.vector.listVectorOf
import org.mechdancer.common.Odometry
import org.mechdancer.common.filters.Differential
import org.mechdancer.common.toPose
import org.mechdancer.geometry.angle.toVector
import org.mechdancer.simulation.Default.newOmniRandomDriving
import org.mechdancer.simulation.Default.remote
import org.mechdancer.simulation.Default.speedSimulation
import org.mechdancer.struct.StructBuilderDSL.Companion.struct
import kotlin.math.PI
import kotlin.math.tan

// 机器人机械结构
private val robot = struct(Chassis()) {
    Encoder(0) asSub { pose(0, +.2, 0) }
    Encoder(1) asSub { pose(0, -.2, PI / 2) }
    Encoder(2) asSub { pose(0, 0, PI / 4) }
}
// 编码器在机器人上的位姿
private val encodersOnRobot =
    robot.devices
        .mapNotNull { (device, tf) -> (device as? Encoder)?.to(tf.toPose()) }
        .toMap()
// 位姿参数
private val parameters =
    encodersOnRobot
        .toList()
        .run {
            listOf(single { (e, _) -> e.key == 0 }.second,
                   single { (e, _) -> e.key == 1 }.second,
                   single { (e, _) -> e.key == 2 }.second)
        }
// 解算矩阵
private val solve =
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

@ExperimentalCoroutinesApi
fun main() = runBlocking {
    // 离散差分环节
    val differential = Differential(robot.what.get()) { _, old, new -> new minusState old }
    val dValue = List(3) { Differential(.0) { _, old, new -> new - old } }
    val random = newOmniRandomDriving()
    var pose = Odometry()
    println(solve)
    speedSimulation(this) {
        random.next()
    }.consumeEach { (_, v) ->
        //  计算机器人位姿增量
        val actual = robot.what.drive(v).data
        actual
            .also { pose -> remote.paint("pose", pose.p.x, pose.p.y, pose.d.asRadian()) }
            .let { differential.update(it) }
            .data
            .let { delta ->
                // 计算编码器增量
                encodersOnRobot
                    .onEach { (encoder, pose) -> encoder.update(pose, delta) }
            }
            .keys
            .let { set -> (0..2).map { i -> dValue[i].update(set.single { it.key == i }.value).data } }
            .let { (v0, v1, v2) ->
                val (u1, u2, theta) = solve * listVectorOf(v0, v1, v2)
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
                pose = pose plusDelta Odometry.odometry(x, y, theta)
//              println("actual = $actual, pose = $pose")
                println("error = ${(actual.p - pose.p).norm()}")
            }
    }
}
