package dora.cache

import dora.cache.repository.BaseMemoryCacheRepository
import java.lang.reflect.InvocationTargetException

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
        val repository = newInstance(repositoryClazz)
        repository?.let {
            val data = it.loadData()
            if (it.cacheName != null && data != null) {
                MemoryCache.updateCacheAtMemory(it.cacheName!!, data)
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
}