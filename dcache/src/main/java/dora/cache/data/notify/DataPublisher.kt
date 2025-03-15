package dora.cache.data.notify

import androidx.lifecycle.MutableLiveData
import java.util.concurrent.ConcurrentHashMap

abstract class DataPublisher<M> : IDataPublisher<M> {

    private var subscriber: IDataSubscriber<M>? = null
    protected val liveDataMap = ConcurrentHashMap<Class<*>, MutableLiveData<Any>>()

    override fun setSubscriber(subscriber: IDataSubscriber<M>) {
        this.subscriber = subscriber
    }

    override fun <T> send(modelType: Class<T>, data: T?) {
        if (liveDataMap.contains(modelType)) {
            liveDataMap[modelType]?.postValue(data)
        } else {
            val liveData = MutableLiveData<Any>()
            liveData.postValue(data as Any)
            liveDataMap[modelType] = liveData
        }
        subscriber?.relay(modelType, this)
    }

    override fun getLiveData(modelType: Class<*>): MutableLiveData<*>? {
        return liveDataMap[modelType]
    }

    override fun getLastValue(modelType: Class<*>): Any? {
        return getLiveData(modelType)?.value
    }
}