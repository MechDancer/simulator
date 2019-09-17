package org.mechdancer.struct

import org.mechdancer.algebra.implement.vector.vector2DOf
import org.mechdancer.common.Odometry
import org.mechdancer.common.toTransformation
import org.mechdancer.geometry.angle.toRad
import org.mechdancer.geometry.transformation.Transformation

class StructBuilder<T>(val what: T) {
    var relationship = Transformation.unit(2)
    private var children = mutableMapOf<Struct<*>, Transformation>()

    fun <T> sub(what: T, block: StructBuilder<T>.() -> Unit) {
        val (child, tf) = StructBuilder(what).apply(block).build()
        children[child] = tf
    }

    fun pose(x: Number, y: Number, theta: Number = 0) {
        relationship = Odometry(vector2DOf(x, y), theta.toRad()).toTransformation()
    }

    private fun build() = Struct(what, *(children.toList().toTypedArray())) to relationship

    companion object {
        fun <T> struct(what: T, block: StructBuilder<T>.() -> Unit) =
            StructBuilder(what).apply(block).build().first
    }
}
