package dora.cache

/**
 * 内存缓存工具。
 */
object MemoryCache {

    private val CACHE: Cache<String, Any> = LruCache(Int.MAX_VALUE)

    fun getCacheFromMemory(name: String): Any? {
        return CACHE.get(name)
    }

    fun removeCacheAtMemory(name: String) {
        if (CACHE.containsKey(name)) {
            CACHE.remove(name)
        }
    }

    /**
     * 推荐使用这个方法而不是[setCacheToMemory]，[updateCacheAtMemory]能保证更新成功。
     *
     * @param name
     * @param cache
     */
    fun updateCacheAtMemory(name: String, cache: Any) {
        removeCacheAtMemory(name)
        setCacheToMemory(name, cache)
    }

    fun cacheKeys(): Set<String> {
        return CACHE.keySet()
    }

    /**
     * 添加缓存，如果name重复则会失败。
     *
     * @param name
     * @param cache
     */
    private fun setCacheToMemory(name: String, cache: Any) {
        CACHE.put(name, cache)
    }
}