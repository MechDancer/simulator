package org.mechdancer.odometry

import org.mechdancer.common.Odometry
import org.mechdancer.odometry.DifferentialOdometry.Encoders.Left
import org.mechdancer.odometry.DifferentialOdometry.Encoders.Right
import org.mechdancer.simulation.Encoder
import org.mechdancer.struct.Struct

/** 宽度为 [width] 的差动底盘里程计 */
class DifferentialOdometry(
    private val width: Double,
    key: (Encoders) -> Any
) {
    enum class Encoders { Left, Right }

    init {
        require(key(Left) != key(Right))
    }

    private var value = Odometry()
    private val left = Encoder(key(Left))
    private val right = Encoder(key(Right))
    private var lLast = .0
    private var rLast = .0

    /**
     * 在机器人运动 [delta] 后更新里程计
     *
     * > 先读取编码器后再通过编码器计算里程，在模拟器里好像是没事找事
     *   但其实是为了获得更真实的误差模型，毕竟计算是不会出现误差的，测量才有误差
     *   如果不能模拟测量这个步骤就不可能模拟出合理的误差
     */
    fun update(delta: Odometry) {
        TODO()
    }

    /** 清零 */
    fun clear() {
        value = Odometry()
    }

    /** 构造里程计机械结构 */
    fun build(): Struct<DifferentialOdometry> {
        TODO()
    }
}
