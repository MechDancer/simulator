package org.mechdancer.simulation.map

import org.mechdancer.algebra.function.vector.div
import org.mechdancer.algebra.implement.matrix.ArrayMatrix
import org.mechdancer.algebra.implement.vector.Vector2D
import org.mechdancer.algebra.implement.vector.vector2D
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 栅格地图
 */
data class GridMap(
    val offset: Vector2D,
    val pixelsPerMeter: Int,
    val data: ArrayMatrix) {
    fun saveToBmp(
        file: File,
        bytesPerPixel: Int,
        pixelToBytes: (p: Double, i: Int) -> Byte
    ) {
        val bytesPerRow = (bytesPerPixel * data.column + 3) / 4 * 4
        val extraPerRow = bytesPerRow - bytesPerPixel * data.column
        val size = bytesPerRow * data.row + 54

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
                putInt(data.column) // 18 : 宽度
                putInt(data.row) // 22 : 高度
                putShort(1) // 26 : 图层数 = 1
                putShort(24) // 28 : 像素长度 = 24
                putInt(0) // 30 : 压缩方式 = 不压缩
                putInt(0) // 34 : 图片大小 = 原始
                putInt(pixelsPerMeter) // 38 : 横向分辨率
                putInt(pixelsPerMeter) // 42 : 纵向分辨率
                putInt(0) // 46 : 调色板
                putInt(0) // 50 : 权
                for (y in 0 until data.row) {
                    for (x in 0 until data.column)
                        for (i in 0 until bytesPerPixel)
                            put(pixelToBytes(data[x, y], i))
                    repeat(extraPerRow) { put(0) }
                }
                putInt((offset.x * pixelsPerMeter).toInt())
                putInt((offset.y * pixelsPerMeter).toInt())
            }
            .array()
            .let(file::writeBytes)
    }

    companion object {
        fun loadFromBmp(
            file: File,
            bytesPerPixel: Int,
            bytesToPixel: (ByteArray) -> Double
        ): GridMap {
            val bytes = file.readBytes()
            val buffer = ByteBuffer
                .allocate(bytes.size)
                .apply {
                    order(ByteOrder.LITTLE_ENDIAN)
                    put(bytes)
                }
            val width = buffer.getInt(18)
            val height = buffer.getInt(22)
            val pixelsPerMeter = buffer.getInt(38)
            require(pixelsPerMeter == buffer.getInt(42))

            val bytesPerRow = (bytesPerPixel * width + 3) / 4 * 4
            val extraPerRow = bytesPerRow - bytesPerPixel * width

            val data = sequence {
                var i = 54
                for (y in 0 until height) {
                    for (x in 0 until width)
                        yield(ByteArray(bytesPerPixel) { buffer.get(i++) }.let(bytesToPixel))
                    i += extraPerRow
                }
            }.toList().toDoubleArray()

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

            return GridMap(vector2D(x0, y0) / pixelsPerMeter,
                           pixelsPerMeter,
                           ArrayMatrix(width, data))
        }
    }
}
