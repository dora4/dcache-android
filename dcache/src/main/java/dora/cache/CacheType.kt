package dora.cache

import android.content.Context

/**
 * ================================================
 * 构建 [Cache] 时,使用 [CacheType] 中声明的类型,来区分不同的模块
 * 从而为不同的模块构建不同的缓存策略
 *
 * @see Cache.Factory.build
 */
interface CacheType {
    /**
     * 返回框架内需要缓存的模块对应的 `id`
     *
     * @return
     */
    val cacheTypeId: Int

    /**
     * 计算对应模块需要的缓存大小
     *
     * @return
     */
    fun calculateCacheSize(context: Context?): Int
}