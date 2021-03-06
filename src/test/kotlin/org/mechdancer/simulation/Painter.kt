package org.mechdancer.simulation

import org.mechdancer.algebra.implement.vector.Vector2D
import org.mechdancer.algebra.implement.vector.Vector3D
import org.mechdancer.common.shape.Polygon
import org.mechdancer.dependency.must
import org.mechdancer.geometry.transformation.Pose2D
import org.mechdancer.remote.presets.RemoteHub
import org.mechdancer.remote.protocol.writeEnd
import org.mechdancer.remote.resources.Command
import org.mechdancer.remote.resources.MulticastSockets
import org.mechdancer.remote.resources.Name
import org.mechdancer.remote.resources.Networks
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

/** 生成网络连接信息字符串 */
fun RemoteHub.networksInfo() =
    with(components) {
        "${must<Name>().field} opened ${must<Networks>().view.size} networks on ${must<MulticastSockets>().address}"
    }

private const val DIR_MASK = 0b0100
private const val FRAME_MASK = 0b1000

private object PaintCommand : Command {
    override val id = 6.toByte()
}

// 画任意内容
private fun RemoteHub.paint(
    topic: String,
    byte: Int,
    block: ByteArrayOutputStream.() -> Unit
) {
    ByteArrayOutputStream()
        .also { stream ->
            stream.writeEnd(topic)
            stream.write(byte)
            stream.block()
        }
        .toByteArray()
        .let { broadcast(PaintCommand, it) }
}

/**
 * 画一维信号
 */
fun RemoteHub.paint(
    topic: String,
    value: Number
) = paint(topic, 1) {
    DataOutputStream(this).apply {
        writeFloat(value.toFloat())
    }
}

/**
 * 画二维信号
 */
fun RemoteHub.paint(
    topic: String,
    x: Number,
    y: Number
) = paint(topic, 2) {
    DataOutputStream(this).apply {
        writeFloat(x.toFloat())
        writeFloat(y.toFloat())
    }
}

/** 画位置信号 */
fun RemoteHub.paint(
    topic: String,
    p: Vector2D
) = paint(topic, p.x, p.y)

fun RemoteHub.paint(
    topic: String,
    shape: Polygon
) = paint(topic, 2 or FRAME_MASK) {
    DataOutputStream(this).apply {
        for ((x, y) in shape.vertex) {
            writeFloat(x.toFloat())
            writeFloat(y.toFloat())
        }
        val (x, y) = shape.vertex.first()
        writeFloat(x.toFloat())
        writeFloat(y.toFloat())
    }
}

/** 画位姿信号 */
fun RemoteHub.paint(
    topic: String,
    pose: Pose2D
) = paint(topic, 2 or DIR_MASK) {
    DataOutputStream(this).apply {
        val (p, d) = pose
        writeFloat(p.x.toFloat())
        writeFloat(p.y.toFloat())
        writeFloat(d.rad.toFloat())
    }
}

/** 画三维位置信号 */
fun RemoteHub.paint(
    topic: String,
    p: Vector3D
) = paint(topic, 3) {
    DataOutputStream(this).apply {
        val (x, y, z) = p
        writeFloat(x.toFloat())
        writeFloat(y.toFloat())
        writeFloat(z.toFloat())
    }
}

/**
 * 画单帧位置信号
 */
fun RemoteHub.paint2DFrame(
    topic: String,
    list: Iterable<Vector2D>
) = paint(topic, 2 or FRAME_MASK) {
    DataOutputStream(this).apply {
        for ((x, y) in list) {
            writeFloat(x.toFloat())
            writeFloat(y.toFloat())
        }
    }
}

/**
 * 画单帧位置信号
 */
fun RemoteHub.paint3DFrame(
    topic: String,
    points: Iterable<Vector3D>
) = paint(topic, 3 or FRAME_MASK) {
    DataOutputStream(this).apply {
        for ((x, y, z) in points) {
            writeFloat(x.toFloat())
            writeFloat(y.toFloat())
            writeFloat(z.toFloat())
        }
    }
}

/**
 * 画单帧位姿信号
 */
fun RemoteHub.paintPoses(
    topic: String,
    list: Iterable<Pose2D>
) = paint(topic, 2 or FRAME_MASK or DIR_MASK) {
    DataOutputStream(this).apply {
        for ((p, d) in list) {
            writeFloat(p.x.toFloat())
            writeFloat(p.y.toFloat())
            writeFloat(d.rad.toFloat())
        }
    }
}
