package com.chengzis.interceptor.demo

import io.github.chengzis.chain.ksp.Chains
import io.github.chengzis.ksp.Define

@Define
@Chains(extend = DefineRadioRepository::class)
interface DefineRadioFavoriteRepository : DefineRadioRepository {

    /**
     * 添加收藏
     * @param frequency 频率
     */
    fun addFavorite2(frequency: Int)
}