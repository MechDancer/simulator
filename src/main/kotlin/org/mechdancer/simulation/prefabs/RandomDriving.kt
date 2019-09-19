package org.mechdancer.simulation.prefabs

import org.mechdancer.common.Velocity

/** 随机行走 */
interface RandomDriving {
    fun next(): Velocity
}
