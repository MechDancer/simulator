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
import org.mechdancer.common.Velocity.Companion.velocity
import org.mechdancer.common.filters.DiscreteDelayer.Companion.delayOn
import org.mechdancer.common.toPose
import org.mechdancer.struct.StructBuilderDSL.Companion.struct
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

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
            pose(0, -0.05, -PI / 4)
        }
    }
}

@ExperimentalCoroutinesApi
fun main() = runBlocking {
    // 编码器在机器人上的位姿
    val encodersOnRobot =
        robot.devices
            .mapNotNull { (device, tf) -> (device as? Encoder<*>)?.to(tf.toPose()) }
            .toMap()
    // 离散延时环节
    val queue = delayOn(robot.what.get())

    produce {
        // 产生随机速度指令
        while (true) {
            send(velocity(0.5 - Random.nextDouble(), PI * (0.5 - Random.nextDouble())))
            delay(100L)
        }
    }.consumeEach { v ->
        //  计算机器人位姿增量
        val current = robot.what.drive(v).data
        val last = queue.update(current)!!.data
        val delta = current minusState last
        // 计算编码器增量
        encodersOnRobot
            .onEach { (encoder, pose) -> encoder.update(pose, delta) }
            .keys
//            .also(::println)
        println("-------------------")
        println("Equation Solve:")
        println(buildInverseEquation().solve()!!.let { Odometry.odometry(it.x,it.y,it.z) })
        println("Actual:")
        println(delta)
        println("-------------------")
        println()
    }
}

fun buildInverseEquation() =
    equations {
        robot.devices
            .mapNotNull { (k, v) -> (k as? Encoder<*>)?.to(v.toPose()) }
            .forEach { (k, v) ->
                this[sin(v.d.asRadian()), cos(v.d.asRadian()), v.p.norm()] = k.value
            }
    }
