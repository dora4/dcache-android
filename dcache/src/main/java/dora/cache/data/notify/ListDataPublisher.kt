package dora.cache.data.notify

import androidx.lifecycle.MutableLiveData
import java.util.concurrent.ConcurrentHashMap

abstract class ListDataPublisher<M> : IListDataPublisher<M> {

    private var subscriber: IListDataSubscriber<M>? = null
    protected val liveDataMap = ConcurrentHashMap<Class<*>, MutableLiveData<MutableList<*>>>()

    override fun setSubscriber(subscriber: IListDataSubscriber<M>) {
        this.subscriber = subscriber
    }

    override fun <T> send(modelType: Class<T>, data: MutableList<T>) {
        if (liveDataMap.contains(modelType)) {
            liveDataMap[modelType]?.postValue(data)
        } else {
            val liveData = MutableLiveData<MutableList<*>>()
            liveData.postValue(data)
            liveDataMap[modelType] = liveData
        }
        subscriber?.relay(modelType, this)
    }

    override fun getListLiveData(modelType: Class<*>): MutableLiveData<MutableList<*>>? {
        return liveDataMap[modelType]
    }

    override fun getLastValue(modelType: Class<*>): MutableList<*>? {
        return getListLiveData(modelType)?.value
    }

    override fun clearLastValue(modelType: Class<*>) {
        getListLiveData(modelType)?.postValue(arrayListOf<M>())
    }
}