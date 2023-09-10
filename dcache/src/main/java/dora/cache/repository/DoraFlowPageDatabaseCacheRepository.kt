package dora.cache.repository

import android.content.Context
import android.util.Log
import dora.cache.holder.ListCacheHolder
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import dora.db.table.OrmTable
import kotlinx.coroutines.flow.MutableStateFlow
import java.lang.IllegalArgumentException

abstract class DoraFlowPageDatabaseCacheRepository<T : OrmTable>(context: Context)
    : DoraFlowDatabaseCacheRepository<T>(context) {

    private var pageNo: Int = 0
    private var pageSize: Int = 10

    fun getPageNo(): Int {
        return pageNo
    }

    fun getPageSize(): Int {
        return pageSize
    }

    open fun setCurrentPage(pageNo: Int, pageSize: Int): DoraFlowPageDatabaseCacheRepository<T> {
        this.pageNo = pageNo
        this.pageSize = pageSize
        return this
    }

    override fun query(): Condition {
        val start = pageNo * pageSize
        val end = start + pageSize
        return QueryBuilder.create()
                .limit(start, end)
                .toCondition()
    }

    /**
     * 没网的情况下直接加载缓存数据。
     */
    override suspend fun selectData(ds: DataSource): Boolean {
        var isLoaded = false
        if (!isNetworkAvailable) {
            isLoaded = ds.loadFromCache(DataSource.CacheType.DATABASE)
        }
        return if (isNetworkAvailable) {
            try {
                ds.loadFromNetwork()
                true
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                isLoaded
            }
        } else isLoaded
    }

    override suspend fun parseModels(models: MutableList<T>?,
                                     liveData: MutableStateFlow<MutableList<T>>) {
        models?.let {
            if (isLogPrint) {
                for (model in it) {
                    Log.d(TAG, "【$description】${model.toString()}")
                }
            }
            onInterceptData(DataSource.Type.NETWORK, it)
            if (!disallowForceUpdate()) {
                if (checkValuesNotNull()) {
                    // 移除之前所有的条件的数据
                    for (condition in (listCacheHolder as ListCacheHolder).cacheConditions) {
                        listCacheHolder.removeOldCache(condition)
                    }
                    listCacheHolder.removeOldCache(query())
                } else throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
            } else {
                if (listDataMap.containsKey(mapKey())) {
                    if (checkValuesNotNull()) {
                        listCacheHolder.removeOldCache(query())
                    } else throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
                } else {
                    listDataMap[mapKey()] = it
                }
            }
            // 追加缓存的模式
            val temp = arrayListOf<T>()
            liveData.value.let { v ->
                temp.addAll(v)
            }
            temp.addAll(it)
            listCacheHolder.addNewCache(temp)
            (listCacheHolder as ListCacheHolder).cacheConditions.add(query())

            if (disallowForceUpdate()) {
                listDataMap[mapKey()]?.let { data -> liveData.emit(data) }
            } else {
                liveData.emit(it)
            }
        }
    }
}