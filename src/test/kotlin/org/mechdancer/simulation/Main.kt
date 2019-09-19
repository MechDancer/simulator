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
import org.mechdancer.common.Odometry
import org.mechdancer.common.filters.DiscreteDelayer.Companion.delayOn
import org.mechdancer.common.toPose
import org.mechdancer.simulation.prefabs.OneStepTransferRandomDrivingBuilderDSL.Companion.oneStepTransferRandomDriving
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
    produce {
        oneStepTransferRandomDriving {
            vx(0.1) {
                row(0.80, 0.20, 0.00)
                row(0.02, 0.80, 0.18)
                row(0.00, 0.20, 0.80)
            }
            vy(0.1) {
                row(0.80, 0.20, 0.00)
                row(0.02, 0.80, 0.18)
                row(0.00, 0.20, 0.80)
            }
            w(0.5) {
                row(0.90, 0.10, 0.00)
                row(0.05, 0.90, 0.05)
                row(0.00, 0.10, 0.90)
            }
        }.run {
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
        val values = encodersOnRobot
            .onEach { (encoder, pose) -> encoder.update(pose, delta) }
            .keys

        println("""
            $values
            ------------------------------
            Equation Solve:
            ${buildInverseEquation().solve()!!.let { Odometry.odometry(it.x, it.y, it.z) }}
            Actual:
            $delta
            
        """.trimIndent())
    }
}

private fun buildInverseEquation() =
    equations {
        encodersOnRobot.forEach { (k, v) ->
            this[sin(v.d.asRadian()), cos(v.d.asRadian()), v.p.norm()] = k.value
        }
    }
