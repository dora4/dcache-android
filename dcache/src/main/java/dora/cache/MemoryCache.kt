package dora.cache

import dora.cache.repository.BaseMemoryCacheRepository
import java.lang.reflect.InvocationTargetException

/**
 * 内存缓存工具。
 */
object MemoryCache {

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
        val repository = newInstance(repositoryClazz)
        repository?.let {
            val data = it.loadCacheInternal()
            if (it.cacheName != null && data != null) {
                updateCacheAtMemory(it.cacheName, data)
            }
        }
    }

    private fun <T> newInstance(clazz: Class<T>): T? {
        val constructors = clazz.declaredConstructors
        for (c in constructors) {
            c.isAccessible = true
            val cls = c.parameterTypes
            if (cls.size == 0) {
                try {
                    return c.newInstance() as T
                } catch (e: InstantiationException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                }
            } else {
                val objs = arrayOfNulls<Any>(cls.size)
                for (i in cls.indices) {
                    objs[i] = getPrimitiveDefaultValue(cls[i])
                }
                try {
                    return c.newInstance(*objs) as T
                } catch (e: InstantiationException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    private fun getPrimitiveDefaultValue(clazz: Class<*>): Any? {
        return if (clazz.isPrimitive) {
            if (clazz == Boolean::class.javaPrimitiveType) false else 0
        } else null
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