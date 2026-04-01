package dora.cache.repository.compose

import android.content.Context
import androidx.lifecycle.viewModelScope
import dora.cache.factory.DatabaseCacheHolderFactory
import dora.cache.holder.DatabaseCacheHolder
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Compose + Database cache repository.
 * 简体中文：Compose + 数据库缓存版本。
 */
abstract class BaseComposeDatabaseRepository<M>(
    context: Context
) : BaseComposeRepository<M>(context) {

    protected abstract fun createCacheHolderFactory(): DatabaseCacheHolderFactory<M>

    protected val cacheHolder: DatabaseCacheHolder<M> by lazy {
        createCacheHolderFactory()
            .createCacheHolder(getModelClass())
            .apply { init() } as DatabaseCacheHolder<M>
    }

    /**
     * Query condition.
     * 简体中文：查询条件。
     */
    protected open fun query(): Condition {
        return QueryBuilder.create().toCondition()
    }

    /**
     * Load from cache.
     * 简体中文：从缓存加载。
     */
    protected open suspend fun loadFromCache(): M? {
        return withContext(Dispatchers.IO) {
            cacheHolder.queryCache(query())
        }
    }

    /**
     * Save to cache.
     * 简体中文：保存缓存。
     */
    protected open suspend fun saveCache(model: M) {
        withContext(Dispatchers.IO) {
            cacheHolder.removeOldCache(query())
            cacheHolder.addNewCache(model)
        }
    }

    /**
     * Compose-style fetch (cache + network).
     * 简体中文：Compose风格获取数据（缓存 + 网络）。
     */
    fun fetchData() {
        viewModelScope.launch {
            _loading.value = true
            val cache = loadFromCache()
            cache?.let {
                _state.value = it
            }
            onLoadFromNetwork()
                .flowOn(Dispatchers.IO)
                .onEach {
                    saveCache(it)
                    _state.value = it
                }
                .catch {
                    _error.emit(it.message ?: "network error")
                }
                .collect()

            _loading.value = false
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getModelClass(): Class<M> {
        return (javaClass.genericSuperclass as java.lang.reflect.ParameterizedType)
            .actualTypeArguments[0] as Class<M>
    }
}