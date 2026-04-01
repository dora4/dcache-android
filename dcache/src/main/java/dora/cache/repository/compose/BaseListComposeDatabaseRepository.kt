package dora.cache.repository.compose

import android.content.Context
import androidx.lifecycle.viewModelScope
import dora.cache.factory.DatabaseCacheHolderFactory
import dora.cache.holder.ListDatabaseCacheHolder
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import kotlinx.coroutines.Dispatchers
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

    protected abstract fun createCacheHolderFactory(): DatabaseCacheHolderFactory<M>

    protected val listCacheHolder: ListDatabaseCacheHolder<M> by lazy {
        createCacheHolderFactory()
            .createCacheHolder(getModelClass())
            .apply { init() } as ListDatabaseCacheHolder<M>
    }

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

    protected open suspend fun saveCacheList(list: List<M>) {
        withContext(Dispatchers.IO) {
            listCacheHolder.removeOldCache(query())
            listCacheHolder.addNewCache(list.toMutableList())
        }
    }

    fun fetchListData() {
        viewModelScope.launch {
            _loading.value = true
            val cache = withContext(Dispatchers.IO) {
                listCacheHolder.queryCache(query())
            }
            if (!cache.isNullOrEmpty()) {
                _state.value = cache
            }
            onLoadFromNetwork()
                .onEach {
                    saveCacheList(it)
                    _state.value = it
                }
                .catch {
                    _error.emit(it.message ?: "error")
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