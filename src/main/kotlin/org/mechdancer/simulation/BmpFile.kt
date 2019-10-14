package org.mechdancer.simulation

import org.mechdancer.algebra.function.vector.div
import org.mechdancer.algebra.function.vector.times
import org.mechdancer.algebra.implement.vector.Vector2D
import org.mechdancer.algebra.implement.vector.vector2DOf
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

private data class Pixel(val x: Int, val y: Int)

fun List<Vector2D>.saveToBmp(
    fileName: String,
    pixelsPerMeter: Int = 100
) {
    var x0 = Int.MAX_VALUE
    var x1 = Int.MIN_VALUE
    var y0 = Int.MAX_VALUE
    var y1 = Int.MIN_VALUE
    val pixels = asSequence()
        .map { it * pixelsPerMeter }
        .map { Pixel((it.x).toInt(), (it.y).toInt()) }
        .onEach { (x, y) ->
            if (x < x0) x0 = x
            else if (x1 < x) x1 = x
            if (y < y0) y0 = y
            else if (y1 < y) y1 = y
        }
        .toList()
    val height = y1 - y0 + 1
    val width = x1 - x0 + 1
    val bytesPerRow = (3 * width + 3) / 4 * 4
    val size = bytesPerRow * height + 54

    val actualName =
        fileName
            .takeIf { it.endsWith(".bmp") }
            ?: "$fileName.bmp"
    ByteBuffer
        .allocate(size + 2 * Int.SIZE_BYTES)
        .apply {
            order(ByteOrder.LITTLE_ENDIAN)
            put(0x42) // 0
            put(0x4D) // 1
            putInt(size) // 2
            putInt(0) // 6
            putInt(54) // 10 : 54 字节
            putInt(40) // 14 : 40 字节
            putInt(width) // 18 : 宽度
            putInt(height) // 22 : 高度
            putShort(1) // 26 : 图层数 = 1
            putShort(24) // 28 : 像素长度 = 24
            putInt(0) // 30 : 压缩方式 = 不压缩
            putInt(0) // 34 : 图片大小 = 原始
            putInt(pixelsPerMeter) // 38 : 横向分辨率
            putInt(pixelsPerMeter) // 42 : 纵向分辨率
            putInt(0) // 46 : 调色板
            putInt(0) // 50 : 权
            putInt(size, x0)
            putInt(y0)
        }
        .array()
        .also { array ->
            val white = ByteArray(3) { 255.toByte() }
            pixels
                .map { (x, y) -> (y - y0) * bytesPerRow + (x - x0) * 3 + 54 }
                .forEach { i -> white.copyInto(array, i) }
        }
        .let(File(actualName)::writeBytes)
}

fun File.loadAsScan(): List<Vector2D> {
    val bytes = readBytes()
    val buffer = ByteBuffer
        .allocate(bytes.size)
        .apply {
            order(ByteOrder.LITTLE_ENDIAN)
            put(bytes)
        }
    val width = buffer.getInt(18)
    val height = buffer.getInt(22)
    val pixelsPerMeter = buffer.getInt(38)
    val bytesPerRow = (3 * width + 3) / 4 * 4

    require(pixelsPerMeter == buffer.getInt(42))
    val x0: Int
    val y0: Int
    when (bytes.size - (bytesPerRow * height + 54)) {
        0                  -> {
            x0 = 0
            y0 = 0
        }
        2 * Int.SIZE_BYTES -> {
            val i = bytes.size - 2 * Int.SIZE_BYTES
            x0 = buffer.getInt(i)
            y0 = buffer.getInt(i + Int.SIZE_BYTES)
        }
        else               ->
            throw IllegalArgumentException("error format in bmp file")
    }

    return sequence {
        for (x in 0 until width)
            for (y in 0 until height) {
                val i = ((y - y0) * bytesPerRow + (x - x0) * 3 + 54)
                if (bytes.copyOfRange(i, i + 3).any { it != 0.toByte() })
                    yield(vector2DOf(x, y) / pixelsPerMeter)
            }
    }.toList()
}
