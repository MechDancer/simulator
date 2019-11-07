package org.mechdancer.simulation

import org.mechdancer.algebra.implement.vector.Vector2D
import org.mechdancer.common.Odometry
import org.mechdancer.common.shape.Polygon
import org.mechdancer.remote.presets.RemoteHub
import org.mechdancer.remote.protocol.writeEnd
import org.mechdancer.remote.resources.Command
import org.mechdancer.simulation.FrameType.*
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import kotlin.concurrent.thread

fun launchBlocking(interval: Long = 0L, block: () -> Any?) =
    if (interval > 0)
        thread {
            while (true) {
                block()
                Thread.sleep(interval)
            }
        }
    else
        thread { while (true) block() }

private object PaintCommand : Command {
    override val id = 6.toByte()
}

/**
 * 画任意内容
 */
fun RemoteHub.paint(
    topic: String,
    block: ByteArrayOutputStream.() -> Unit
) {
    ByteArrayOutputStream()
        .also { stream ->
            stream.writeEnd(topic)
            stream.block()
        }
        .toByteArray()
        .let { broadcast(PaintCommand, it) }
}

enum class FrameType(val value: Int) {
    OneFloat(0),
    OneDouble(1),
    TwoFloat(2),
    TwoDouble(3),
    ThreeFloat(4),
    ThreeDouble(5)
}

/**
 * 画一维信号
 */
fun RemoteHub.paint(
    topic: String,
    value: Double
) = paint(topic) {
    DataOutputStream(this).apply {
        writeByte(1)
        writeDouble(value)
    }
}

/**
 * 画二维信号
 */
fun RemoteHub.paint(
    topic: String,
    x: Double,
    y: Double
) = paint(topic) {
    DataOutputStream(this).apply {
        writeByte(2)
        writeDouble(x)
        writeDouble(y)
    }
}

/**
 * 画位姿信号
 */
fun RemoteHub.paint(
    topic: String,
    x: Double,
    y: Double,
    theta: Double
) = paint(topic) {
    DataOutputStream(this).apply {
        writeByte(3)
        writeDouble(x)
        writeDouble(y)
        writeDouble(theta)
    }
}

/**
 * 画位姿信号
 */
fun RemoteHub.paintPose(
    topic: String,
    pose: Odometry
) = paint(topic) {
    DataOutputStream(this).apply {
        writeByte(3)
        writeDouble(pose.p.x)
        writeDouble(pose.p.y)
        writeDouble(pose.d.asRadian())
    }
}

/**
 * 画单帧一维信号
 */
fun RemoteHub.paintFrame1(
    topic: String,
    list: List<Double>
) = paint(topic) {
    DataOutputStream(this).apply {
        writeByte(0)
        writeByte(OneDouble.value)
        list.forEach(this::writeDouble)
    }
}

/**
 * 画单帧二维信号
 */
fun RemoteHub.paintFrame2(
    topic: String,
    list: List<Pair<Double, Double>>
) = paint(topic) {
    DataOutputStream(this).apply {
        writeByte(0)
        writeByte(TwoDouble.value)
        for ((x, y) in list) {
            writeDouble(x)
            writeDouble(y)
        }
    }
}

/**
 * 画单帧位姿信号
 */
fun RemoteHub.paintFrame3(
    topic: String,
    list: List<Triple<Double, Double, Double>>
) = paint(topic) {
    DataOutputStream(this).apply {
        writeByte(0)
        writeByte(ThreeDouble.value)
        for ((x, y, theta) in list) {
            writeDouble(x)
            writeDouble(y)
            writeDouble(theta)
        }
    }
}

/**
 * 画单帧向量信号
 */
fun RemoteHub.paintVectors(
    topic: String,
    list: Collection<Vector2D>
) = paint(topic) {
    DataOutputStream(this).apply {
        writeByte(0)
        writeByte(TwoDouble.value)
        for ((x, y) in list) {
            writeDouble(x)
            writeDouble(y)
        }
    }
}

/**
 * 画单帧位姿信号
 */
fun RemoteHub.paintPoses(
    topic: String,
    list: List<Odometry>
) = paint(topic) {
    DataOutputStream(this).apply {
        writeByte(0)
        writeByte(ThreeDouble.value)
        for ((p, d) in list) {
            writeDouble(p.x)
            writeDouble(p.y)
            writeDouble(d.asRadian())
        }
    }
}

fun RemoteHub.paint(
    topic: String,
    shape: Polygon
) = paint(topic) {
    DataOutputStream(this).apply {
        writeByte(0)
        writeByte(TwoDouble.value)
        for ((x, y) in shape.vertex) {
            writeDouble(x)
            writeDouble(y)
        }
        with(shape.vertex.first()) {
            writeDouble(x)
            writeDouble(y)
        }
    }
}
