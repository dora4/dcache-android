package dora.cache.repository

import android.content.Context
import androidx.lifecycle.LiveData
import dora.cache.data.DataFetcher
import dora.cache.data.ListDataFetcher
import dora.cache.data.page.DataPager
import dora.cache.data.page.IDataPager
import dora.http.DoraCallback
import dora.http.DoraListCallback

abstract class BaseNoCacheRepository<M> protected constructor(context: Context) :
        BaseRepository<M>(context) {

    override fun installDataFetcher(): DataFetcher<M> {
        return object : DataFetcher<M>() {
            override fun fetchData(): LiveData<M> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType?): Boolean {
                        return false
                    }

                    override fun loadFromNetwork() {
                        onLoadFromNetwork(callback())
                    }
                })
                return liveData
            }

            override fun callback(): DoraCallback<M> {
                return object : DoraCallback<M>() {
                    override fun onSuccess(data: M) {
                        onInterceptNetworkData(data)
                        liveData.value = data
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isClearDataOnNetworkError) {
                            liveData.value = null
                        }
                    }

                    override fun onInterceptNetworkData(data: M) {
                        onInterceptData(DataSource.Type.NETWORK, data)
                    }
                }
            }
        }
    }

    override fun installListDataFetcher(): ListDataFetcher<M> {
        return object : ListDataFetcher<M>() {

            override fun fetchListData(): LiveData<List<M>> {
                selectData(object : DataSource {
                    override fun loadFromCache(type: DataSource.CacheType?): Boolean {
                        return false
                    }

                    override fun loadFromNetwork() {
                        onLoadFromNetwork(listCallback())
                    }
                })
                return liveData
            }

            override fun listCallback(): DoraListCallback<M> {
                return object : DoraListCallback<M>() {
                    override fun onSuccess(data: List<M>) {
                        onInterceptNetworkData(data)
                        liveData.value = data
                    }

                    override fun onFailure(code: Int, msg: String?) {
                        if (isClearDataOnNetworkError) {
                            liveData.value = null
                        }
                    }

                    override fun onInterceptNetworkData(data: List<M>) {
                        onInterceptData(DataSource.Type.NETWORK, data)
                    }
                }
            }

            override fun obtainPager(): IDataPager<M> {
                return DataPager(liveData.value as List<M>)
            }
        }
    }

    protected fun onInterceptData(type: DataSource.Type, data: M) {}
    protected fun onInterceptData(type: DataSource.Type, data: List<M>) {}
}