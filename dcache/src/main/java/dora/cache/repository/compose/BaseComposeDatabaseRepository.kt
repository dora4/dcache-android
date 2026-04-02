package dora.cache.repository.compose

import android.content.Context
import androidx.lifecycle.viewModelScope
import dora.cache.holder.DatabaseCacheHolder
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

    /**
     * Cache holder.
     * 简体中文：缓存持有者（懒加载）。
     */
    protected val cacheHolder: DatabaseCacheHolder<M> by lazy {
        createCacheHolder().apply { init() }
    }

    /**
     * Create cache holder.
     * 简体中文：创建缓存持有者。
     */
    protected abstract fun createCacheHolder(): DatabaseCacheHolder<M>

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
     * Fetch job.
     * 简体中文：请求任务（防止重复请求）。
     */
    private var fetchJob: Job? = null

    /**
     * Fetch data.
     * 简体中文：获取数据（缓存 + 网络）。
     */
    fun fetchData() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _loading.value = true
            try {
                // Load cache first.
                // 简体中文：优先加载缓存。
                val cache = loadFromCache()
                cache?.let {
                    _state.value = it
                }
                // Load from network.
                // 简体中文：再请求网络。
                onLoadFromNetwork()
                    .onEach {
                        saveCache(it)
                        _state.value = it
                    }
                    .catch {
                        _error.emit(it.message ?: "error")
                    }
                    .collect()
            } finally {
                _loading.value = false
            }
        }
    }
}