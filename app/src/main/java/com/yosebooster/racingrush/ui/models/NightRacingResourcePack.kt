package com.yosebooster.racingrush.ui.models

import com.yosebooster.racingrush.R

data class NightRacingResourcePack(
    override val backgroundImageDrawable: Int = R.drawable.bg_road_night,
    override val carImageDrawable: Int = R.drawable.ic_car,
    override val blockerImageDrawable: Int = R.drawable.ic_block_night
) : RacingResourcePack