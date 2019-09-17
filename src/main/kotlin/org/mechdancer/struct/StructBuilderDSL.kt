package org.mechdancer.struct

import org.mechdancer.geometry.transformation.Transformation

/**
 * 结构体构建器
 */
class StructBuilderDSL<T> private constructor(private val root: T) : RelationRecorderDSL() {
    private var children = mutableMapOf<Struct<*>, Transformation>()

    fun <U> sub(what: U, block: StructBuilderDSL<U>.() -> Unit) {
        val (child, tf) = StructBuilderDSL(what).apply(block).build()
        children[child] = tf
    }

    fun sub(child: Struct<*>, block: RelationRecorderDSL.() -> Unit) {
        children[child] = RelationRecorderDSL().apply(block).relation
    }

    private fun build() = Struct(root, *(children.toList().toTypedArray())) to relation

    companion object {
        fun <T> struct(what: T, block: StructBuilderDSL<T>.() -> Unit) =
            StructBuilderDSL(what).apply(block).build().first
    }
}
