package org.mechdancer.common

import org.mechdancer.algebra.function.vector.minus
import org.mechdancer.algebra.function.vector.plus
import org.mechdancer.algebra.implement.vector.Vector2D
import org.mechdancer.algebra.implement.vector.vector2DOf
import org.mechdancer.algebra.implement.vector.vector2DOfZero
import org.mechdancer.geometry.angle.*

/**
 * 里程计
 * @param p 位置
 * @param d 方向
 */
data class Odometry(
    val p: Vector2D = vector2DOfZero(),
    val d: Angle = .0.toRad()
) {
    /** 增量 [delta] 累加到里程 */
    infix fun plusDelta(delta: Odometry) =
        Odometry(p + delta.p.rotate(d),
                 d rotate delta.d)

    /** 里程回滚到增量 [delta] 之前 */
    infix fun minusDelta(delta: Odometry) =
        (d rotate -delta.d)
            .let { Odometry(p - delta.p.rotate(-it), it) }

    /** 计算里程从标记 [mark] 到当前状态的增量 */
    infix fun minusState(mark: Odometry) =
        Odometry((p - mark.p).rotate(-mark.d),
                 d.rotate(-mark.d).adjust())

    override fun toString() = "(${p.x}, ${p.y})($d)"

    companion object {
        fun odometry(x: Number, y: Number, theta: Number = 0) =
            Odometry(vector2DOf(x, y), theta.toRad())
    }
}
