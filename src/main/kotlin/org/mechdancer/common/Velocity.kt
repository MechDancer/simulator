package org.mechdancer.common

/** 标准底盘运动学模型下的控制量 */
sealed class Velocity {
    /** 静止不动 */
    object Static : Velocity()

    /** 非全向 */
    data class NonOmnidirectional(
        val v: Double,
        val w: Double
    ) : Velocity()

    /** 全向 */
    data class Omnidirectional(
        val vx: Double,
        val vy: Double,
        val w: Double
    ) : Velocity()
}
