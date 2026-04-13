package dora.cache.repository.compose

import android.content.Context
import androidx.lifecycle.viewModelScope
import dora.cache.data.fetcher.OnLoadListener
import dora.cache.holder.DatabaseCacheHolder
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Compose + Database cache.
 * 简体中文：Compose + 数据库缓存。
 */
abstract class BaseComposeDatabaseRepository<M>(
    context: Context
) : BaseComposeRepository<M>(context) {

    /**
     * Fetch job.
     * 简体中文：请求任务（防止重复请求）。
     */
    private var fetchJob: Job? = null

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
     * Fetch data.
     * 简体中文：获取数据。
     */
    override fun fetchData(listener: OnLoadListener?) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _loading.value = true
            try {
                // Load cache first.
                // 简体中文：优先加载缓存。
                val cache = loadFromCache()
                if (cache != null) {
                    onInterceptData(DataSource.Type.CACHE, cache)
                    _state.value = cache
                    listener?.onLoad(OnLoadListener.Source.CACHE, OnLoadListener.SUCCESS)
                } else {
                    listener?.onLoad(OnLoadListener.Source.CACHE, OnLoadListener.FAILURE)
                }
                // Load from network.
                // 简体中文：再请求网络。
                onLoadFromNetwork()
                    .onEach {
                        onInterceptData(DataSource.Type.NETWORK, it)
                        saveCache(it)
                        _state.value = it
                        listener?.onLoad(
                            OnLoadListener.Source.NETWORK,
                            OnLoadListener.SUCCESS
                        )
                    }
                    .catch {
                        _error.emit(it.message ?: "error")
                        listener?.onLoad(
                            OnLoadListener.Source.NETWORK,
                            OnLoadListener.FAILURE
                        )
                    }
                    .collect()
            } finally {
                _loading.value = false
            }
        }
    }
}