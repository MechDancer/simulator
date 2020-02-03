package org.mechdancer.common.shape

import org.mechdancer.algebra.implement.vector.Vector2D

/** 形状 */
interface Shape {
    /** 面积 */
    val area: Double

    /** 判定点在形状内部 */
    operator fun contains(p: Vector2D): Boolean
}
