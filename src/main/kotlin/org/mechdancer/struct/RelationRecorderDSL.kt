package org.mechdancer.struct

import org.mechdancer.geometry.transformation.*

/**
 * 关系记录器
 */
open class RelationRecorderDSL internal constructor() {
    var relation = MatrixTransformation.unit(2)
    var pose: Pose2D
        get() = relation.toPose2D()
        set(value) {
            relation = value.toTransformation()
        }

    fun where(x: Number, y: Number, theta: Number = 0) {
        relation = pose2D(x, y, theta).toTransformation()
    }
}
