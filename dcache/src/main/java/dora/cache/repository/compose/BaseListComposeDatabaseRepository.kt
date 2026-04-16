package dora.cache.repository.compose

import android.content.Context
import androidx.lifecycle.viewModelScope
import dora.cache.data.fetcher.OnLoadListener
import dora.cache.holder.ListDatabaseCacheHolder
import dora.cache.repository.compose.flow.UiEvent
import dora.cache.repository.compose.flow.UiState
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Compose + Database cache (List version).
 * 简体中文：Compose + 数据库缓存（列表版）。
 */
abstract class BaseListComposeDatabaseRepository<M>(
    context: Context
) : BaseListComposeRepository<M>(context) {

    /**
     * Fetch job.
     * 简体中文：请求任务（防止重复请求）。
     */
    private var fetchJob: Job? = null

    /**
     * Cache holder.
     * 简体中文：缓存持有者（懒加载）。
     */
    protected open val listCacheHolder: ListDatabaseCacheHolder<M> by lazy {
        createCacheHolder().apply { init() }
    }

    /**
     * Create cache holder.
     * 简体中文：创建缓存持有者。
     */
    protected abstract fun createCacheHolder(): ListDatabaseCacheHolder<M>

    /**
     * Query condition.
     * 简体中文：查询条件。
     */
    protected open fun query(): Condition {
        return QueryBuilder.create().toCondition()
    }

    /**
     * Load list from cache.
     * 简体中文：从缓存加载列表。
     */
    protected open suspend fun loadFromCacheList(): List<M>? {
        return withContext(Dispatchers.IO) {
            listCacheHolder.queryCache(query())
        }
    }

    /**
     * Save list to cache.
     * 简体中文：保存列表到缓存。
     */
    protected open suspend fun saveCacheList(list: List<M>) {
        withContext(Dispatchers.IO) {
            listCacheHolder.removeOldCache(query())
            listCacheHolder.addNewCache(list.toMutableList())
        }
    }

    /**
     * Fetch list data.
     * 简体中文：获取列表数据。
     */
    override fun fetchListData(listener: OnLoadListener?) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _state.value = UiState.Loading
            // Load cache first.
            // 简体中文：优先加载缓存。
            val cache = loadFromCacheList()
            if (cache != null) {
                if (cache.isNotEmpty()) {
                    onInterceptData(DataSource.Type.CACHE, cache.toMutableList())
                    _state.value = UiState.Success(cache, UiState.Source.CACHE)
                    listener?.onLoad(OnLoadListener.Source.CACHE, OnLoadListener.SUCCESS)
                }
            } else {
                listener?.onLoad(OnLoadListener.Source.CACHE, OnLoadListener.FAILURE)
            }
            // Load from network.
            // 简体中文：再请求网络。
            onLoadFromNetwork()
                .onEach {
                    onInterceptData(DataSource.Type.NETWORK, it.toMutableList())
                    saveCacheList(it)
                    if (it.isEmpty()) {
                        _state.value = UiState.Empty
                    } else {
                        _state.value = UiState.Success(it, UiState.Source.NETWORK)
                    }
                    listener?.onLoad(
                        OnLoadListener.Source.NETWORK,
                        OnLoadListener.SUCCESS
                    )
                }
                .catch {
                    _event.emit(UiEvent.Toast(it.message ?: "error"))
                    listener?.onLoad(
                        OnLoadListener.Source.NETWORK,
                        OnLoadListener.FAILURE
                    )
                }
                .collect()
        }
    }
}