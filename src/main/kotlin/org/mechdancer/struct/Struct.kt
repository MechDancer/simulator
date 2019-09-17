package org.mechdancer.struct

import org.mechdancer.geometry.transformation.Transformation

/**
 * 机械结构体
 * 结构体与其子结构体具有固定的连接
 */
class Struct<T>(
    val what: T,
    vararg children: Pair<Struct<*>, Transformation>
) {
    private val children = children.toMap()

    val devices: Map<Struct<*>, Transformation> by lazy {
        this.children
            .flatMap { (child, childToThis) ->
                child.devices.map { (child, deviceToChild) ->
                    child to childToThis * deviceToChild
                }
            }
            .let { it + (this to Transformation.unit(2)) }
            .toMap()
    }
}
