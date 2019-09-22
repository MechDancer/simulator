package org.mechdancer.simulation.prefabs

import org.mechdancer.algebra.core.Matrix
import org.mechdancer.algebra.doubleEquals
import org.mechdancer.algebra.function.matrix.dim
import org.mechdancer.algebra.implement.matrix.builder.BuilderMode.Immutable
import org.mechdancer.algebra.implement.matrix.builder.MatrixBuilder
import org.mechdancer.algebra.implement.matrix.builder.arrayMatrixOfUnit
import org.mechdancer.algebra.implement.matrix.builder.matrix

class OneStepTransferRandomDrivingBuilderDSL
private constructor() {
    var vx = .1
    var vy = .0
    var w = .5
    var vxMatrix: Matrix = arrayMatrixOfUnit(3)
    var vyMatrix: Matrix = arrayMatrixOfUnit(3)
    var wMatrix: Matrix = arrayMatrixOfUnit(3)

    fun vx(value: Number, block: MatrixBuilder.() -> Unit) {
        vx = value.toDouble()
        vxMatrix = matrix(Immutable, block)
    }

    fun vy(value: Number, block: MatrixBuilder.() -> Unit) {
        vy = value.toDouble()
        vyMatrix = matrix(Immutable, block)
    }

    fun w(value: Number, block: MatrixBuilder.() -> Unit) {
        w = value.toDouble()
        wMatrix = matrix(Immutable, block)
    }

    companion object {
        private fun Matrix.assert() {
            require(dim == 3)
            for (r in rows)
                require(doubleEquals(1.0, r.toList().sum()))
        }

        fun oneStepTransferRandomDriving(
            block: OneStepTransferRandomDrivingBuilderDSL.() -> Unit = {}
        ) = OneStepTransferRandomDrivingBuilderDSL()
            .apply(block)
            .apply {
                require(vx >= 0)
                require(vy >= 0)
                require(w >= 0)
                vxMatrix.assert()
                vyMatrix.assert()
                wMatrix.assert()
            }.run {
                OneStepTransferRandomDriving(vx, vy, w, vxMatrix, vyMatrix, wMatrix)
            }
    }
}
