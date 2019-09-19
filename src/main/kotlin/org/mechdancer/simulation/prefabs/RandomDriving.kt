package org.mechdancer.simulation.prefabs

import org.mechdancer.algebra.core.Matrix
import org.mechdancer.algebra.function.matrix.dim
import org.mechdancer.common.Velocity.Companion.velocity
import org.mechdancer.common.Velocity.NonOmnidirectional
import kotlin.math.sign
import kotlin.random.Random

class RandomDriving(
    private val v: Double,
    private val w: Double,
    private val vMatrix: Matrix,
    private val wMatrix: Matrix
) {
    private var vState = 0
    private var wState = 0

    fun next(): NonOmnidirectional {
        // 状态转移
        vState = vMatrix.transfer(vState)
        wState = wMatrix.transfer(wState)
        return velocity((vState - 1).sign * v, (wState - 1).sign * w)
    }

    companion object {
        private fun Matrix.transfer(current: Int): Int {
            require(current in 0 until dim)
            var value = Random.nextDouble()
            row(current).toList().forEachIndexed { i, p ->
                value -= p
                if (value <= 0) return i
            }
            throw RuntimeException(value.toString())
        }
    }
}
