package org.mechdancer.simulation.prefabs

import org.mechdancer.algebra.core.Matrix
import org.mechdancer.algebra.function.matrix.dim
import org.mechdancer.algebra.function.matrix.power
import org.mechdancer.common.Velocity.Companion.velocity
import org.mechdancer.common.Velocity.Omnidirectional
import kotlin.math.sign
import kotlin.random.Random

/** 一步转移概率驱动的全向随机行走 */
class OneStepTransferRandomDriving
internal constructor(
    private val vx: Double,
    private val vy: Double,
    private val w: Double,
    private val vxMatrix: Matrix,
    private val vyMatrix: Matrix,
    private val wMatrix: Matrix
) {
    private var vxState = 1
    private var vyState = 1
    private var wState = 1

    infix fun power(k: Int): OneStepTransferRandomDriving {
        require(k > 0)
        return OneStepTransferRandomDriving(
            vx, vy, w,
            vxMatrix power k,
            vyMatrix power k,
            wMatrix power k)
    }

    fun next(): Omnidirectional {
        // 状态转移
        vxState = vxMatrix.transfer(vxState)
        vyState = vyMatrix.transfer(vyState)
        wState = wMatrix.transfer(wState)
        return velocity((vxState - 1).sign * vx,
                        (vyState - 1).sign * vy,
                        (wState - 1).sign * w)
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
