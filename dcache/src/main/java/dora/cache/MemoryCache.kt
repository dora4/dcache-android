package dora.cache

import android.content.Context
import android.text.TextUtils
import dora.cache.repository.BaseMemoryCacheRepository

/**
 * 内存缓存工具。
 */
object MemoryCache {

    /**
     * 一般在Application中调用。
     *
     * @param repositories
     */
    fun scan(context: Context, vararg repositories: Class<out BaseMemoryCacheRepository<*>>) {
        for (repositoryClazz in repositories) {
            loadCache(context, repositoryClazz)
        }
    }

    /**
     * App启动时把部分仓库的数据加载到内存。
     *
     * @param repositoryClazz
     */
    private fun loadCache(context: Context, repositoryClazz: Class<out BaseMemoryCacheRepository<*>>) {
        val repository = repositoryClazz.getConstructor(Context::class.java).newInstance(context)
        repository?.let {
            val data = it.loadCacheInternal()
            if (TextUtils.isEmpty(it.cacheName)) {
                it.cacheName = repositoryClazz.simpleName
            }
            if (data != null) {
                updateCacheAtMemory(it.cacheName, data)
            }
        }
    }

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
     * 推荐使用这个方法而不是[putCacheToMemory]，[updateCacheAtMemory]能保证更新成功。
     *
     * @param name
     * @param cache
     */
    fun updateCacheAtMemory(name: String, cache: Any) {
        removeCacheAtMemory(name)
        putCacheToMemory(name, cache)
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
    private fun putCacheToMemory(name: String, cache: Any) {
        CACHE.put(name, cache)
    }
}