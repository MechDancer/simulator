package org.mechdancer.struct

import org.mechdancer.common.Odometry
import org.mechdancer.common.toPose
import org.mechdancer.common.toTransformation
import org.mechdancer.geometry.transformation.Transformation

/**
 * 关系记录器
 */
open class RelationRecorderDSL internal constructor() {
    var relation = Transformation.unit(2)
    var pose: Odometry
        get() = relation.toPose()
        set(value) {
            relation = value.toTransformation()
        }

    fun pose(x: Number, y: Number, theta: Number = 0) {
        relation = Odometry.pose(x, y, theta).toTransformation()
    }
}
