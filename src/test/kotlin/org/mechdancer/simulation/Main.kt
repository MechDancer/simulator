package org.mechdancer.simulation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.mechdancer.algebra.function.equation.solve
import org.mechdancer.algebra.function.vector.norm
import org.mechdancer.algebra.function.vector.x
import org.mechdancer.algebra.function.vector.y
import org.mechdancer.algebra.function.vector.z
import org.mechdancer.algebra.implement.equation.builder.equations
import org.mechdancer.algebra.implement.matrix.builder.matrix
import org.mechdancer.common.Odometry
import org.mechdancer.common.filters.DiscreteDelayer.Companion.delayOn
import org.mechdancer.common.toPose
import org.mechdancer.struct.StructBuilderDSL.Companion.struct
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// 机器人机械结构
private val robot = struct(Chassis()) {
    "定位模块" asSub {
        pose(0.31, 0)
        Encoder(0) asSub {
            pose(0, 0.05, PI)
        }
        Encoder(1) asSub {
            pose(0.05, 0, PI / 2)
        }
        Encoder(2) asSub {
            pose(0, -0.05, PI / 4)
        }
    }
}

// 编码器在机器人上的位姿
private val encodersOnRobot =
    robot.devices
        .mapNotNull { (device, tf) -> (device as? Encoder<*>)?.to(tf.toPose()) }
        .toMap()

@ExperimentalCoroutinesApi
fun main() = runBlocking {
    // 离散延时环节
    val queue = delayOn(robot.what.get())
    val fSum = Odometry()
    produce {
        Default.newRandomDriving()
            .run {
                while (true) {
                    send(next())
                    delay(100L)
                }
            }
    }.consumeEach { v ->
        //  计算机器人位姿增量
        val current = robot.what.drive(v).data
            .also { pose -> Default.remote.paint("pose", pose.p.x, pose.p.y, pose.d.asRadian()) }
        val last = queue.update(current)!!.data
        val delta = current minusState last
        // 计算编码器增量
        val save = encodersOnRobot
            .keys
            .associateWith { it.value }
        val values = encodersOnRobot
            .onEach { (encoder, pose) -> encoder.update(pose, delta) }
            .keys
        val deltas = encodersOnRobot
            .keys
            .associateWith { it.value - save.getValue(it) }.values

        val fSolve = buildInverseEquation().solve()!!.let { Odometry.odometry(it.x, it.y, it.z) }
        fSum.plusDelta(fSolve)

        println(
            """
            $values
            ------------------------------
            Equation Solve:
            $fSum
            Actual:
            $current

        """.trimIndent()
        )
    }
}

// (e1, e2 ,e3) -> (x, y, w)
private fun buildInverseEquation() =
    equations {
        encodersOnRobot.forEach { (k, v) ->
            this[1 safeDivide sin(v.d.asRadian()), 1 safeDivide cos(v.d.asRadian()), v.p.norm()] = k.value
        }
    }

// (x, y, w) -> (e1, e2 ,e3)
private val encoderTransformation = matrix {
    encodersOnRobot.forEach { (_, v) ->
        row(1 safeDivide sin(v.d.asRadian()), 1 safeDivide cos(v.d.asRadian()), v.p.norm())
    }
}

private infix fun Number.safeDivide(other: Number) =
    if (other.toDouble() == .0)
        .0
    else this.toDouble() / other.toDouble()