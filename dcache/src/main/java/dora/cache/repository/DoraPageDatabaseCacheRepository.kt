package dora.cache.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import dora.cache.holder.ListDatabaseCacheHolder
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import dora.db.table.OrmTable
import java.lang.IllegalArgumentException

abstract class DoraPageDatabaseCacheRepository<T : OrmTable>(context: Context)
    : DoraDatabaseCacheRepository<T>(context) {

    private var pageNo: Int = 0
    private var pageSize: Int = 10

    fun getPageNo(): Int {
        return pageNo
    }

    fun getPageSize(): Int {
        return pageSize
    }

    open fun setCurrentPage(pageNo: Int, pageSize: Int): DoraPageDatabaseCacheRepository<T> {
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
    override fun selectData(ds: DataSource): Boolean {
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

    override fun parseModels(models: MutableList<T>?,
                                   liveData: MutableLiveData<MutableList<T>>) {
        models?.let {
            if (isLogPrint) {
                for (model in it) {
                    Log.d(TAG, "【$description】$model")
                }
            }
            onInterceptData(DataSource.Type.NETWORK, it)
            if (!checkValuesNotNull()) {
                throw IllegalArgumentException("Query parameter would be null, checkValuesNotNull return false.")
            }
            if (!disallowForceUpdate()) {
                // 移除之前所有的条件的数据
                for (condition in (listCacheHolder as ListDatabaseCacheHolder).cacheConditions) {
                    (listCacheHolder as ListDatabaseCacheHolder<T>).removeOldCache(condition)
                }
                (listCacheHolder as ListDatabaseCacheHolder<T>).removeOldCache(query())
            } else {
                if (listDataMap.containsKey(mapKey())) {
                    (listCacheHolder as ListDatabaseCacheHolder<T>).removeOldCache(query())
                } else {
                    listDataMap[mapKey()] = it
                }
            }
            // 追加缓存的模式
            val temp = arrayListOf<T>()
            liveData.value?.let { v ->
                temp.addAll(v)
            }
            temp.addAll(it)
            (listCacheHolder as ListDatabaseCacheHolder<T>).addNewCache(temp)
            (listCacheHolder as ListDatabaseCacheHolder).cacheConditions.add(query())

            if (disallowForceUpdate()) {
                liveData.postValue(listDataMap[mapKey()])
            } else {
                liveData.postValue(it)
            }
        }
    }
}