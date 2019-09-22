package org.mechdancer.simulation.prefabs

import org.mechdancer.algebra.function.matrix.dim
import org.mechdancer.algebra.function.matrix.toList
import org.mechdancer.algebra.implement.matrix.ArrayMatrix
import org.mechdancer.algebra.implement.matrix.builder.BuilderMode.ValueMutable
import org.mechdancer.algebra.implement.matrix.builder.MatrixBuilder
import org.mechdancer.algebra.implement.matrix.builder.arrayMatrixOfUnit
import org.mechdancer.algebra.implement.matrix.builder.matrix
import org.mechdancer.algebra.implement.matrix.builder.toArrayMatrix

class OneStepTransferRandomDrivingBuilderDSL
private constructor() {
    var steps = 1
    var vx = .1
    var vy = .0
    var w = .5
    var vxMatrix = arrayMatrixOfUnit(3)
    var vyMatrix = arrayMatrixOfUnit(3)
    var wMatrix = arrayMatrixOfUnit(3)

    fun vx(value: Number, block: MatrixBuilder.() -> Unit) {
        vx = value.toDouble()
        vxMatrix = matrix(ValueMutable, block).toArrayMatrix().apply { complete() }
    }

    fun vy(value: Number, block: MatrixBuilder.() -> Unit) {
        vy = value.toDouble()
        vyMatrix = matrix(ValueMutable, block).toArrayMatrix().apply { complete() }
    }

    fun w(value: Number, block: MatrixBuilder.() -> Unit) {
        w = value.toDouble()
        wMatrix = matrix(ValueMutable, block).toArrayMatrix().apply { complete() }
    }

    private fun ArrayMatrix.complete() {
        require(dim == 3)
        for (r in 0..2) set(r, r, -row(r).toList().sum())
        require(toList().all { it >= 0 })
    }

    companion object {
        fun oneStepTransferRandomDriving(
            block: OneStepTransferRandomDrivingBuilderDSL.() -> Unit = {}
        ) =
            OneStepTransferRandomDrivingBuilderDSL()
                .apply(block)
                .apply {
                    require(vx >= 0)
                    require(vy >= 0)
                    require(w >= 0)
                }.run {
                    OneStepTransferRandomDriving(vx, vy, w, vxMatrix, vyMatrix, wMatrix) power steps
                }
    }
}
