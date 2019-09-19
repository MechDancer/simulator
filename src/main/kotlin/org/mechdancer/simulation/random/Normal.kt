package org.mechdancer.simulation.random

object Normal {
    private val random = java.util.Random()

    /** 按期望 [expect] 和 标准差 [sigma] 获取一个随机数 */
    fun next(expect: Double = 0.0, sigma: Double = 1.0) =
        random.nextGaussian() * sigma + expect
}
