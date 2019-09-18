package org.mechdancer.struct

import org.mechdancer.geometry.transformation.Transformation

/**
 * 机械结构关系
 * 结构体与其子结构体具有固定的连接
 */
class Struct<T>(
    val what: T,
    vararg children: Pair<Struct<*>, Transformation>
) {
    private val children = children.toMap()

    /** 计算并缓存结构体到其所有各级子结构体的变换关系 */
    val devices: Map<*, Transformation> by lazy {
        this.children
            .asSequence()
            .flatMap { (child, childToThis) ->
                child.devices.asSequence()
                    .map { (child, deviceToChild) ->
                        child to childToThis * deviceToChild
                    }
            }
            .let { it + (what to Transformation.unit(2)) }
            .toMap()
    }
}
