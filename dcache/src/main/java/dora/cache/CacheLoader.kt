package dora.cache

import dora.cache.repository.BaseMemoryCacheRepository
import dora.util.KeyValueUtils
import dora.util.ReflectionUtils

object CacheLoader {
    /**
     * 一般在Application中调用。
     *
     * @param repositories
     */
    fun scan(vararg repositories: Class<out BaseMemoryCacheRepository<*>>) {
        for (repositoryClazz in repositories) {
            loadCache(repositoryClazz)
        }
    }

    /**
     * App启动时把部分仓库的数据加载到内存。
     *
     * @param repositoryClazz
     */
    private fun loadCache(repositoryClazz: Class<out BaseMemoryCacheRepository<*>>) {
        val repository = ReflectionUtils.newInstance(repositoryClazz)
        repository?.let {
            val data = it.loadData()
            if (it.cacheName != null && data != null) {
                KeyValueUtils.updateCacheAtMemory(it.cacheName!!, data)
            }
        }
    }
}