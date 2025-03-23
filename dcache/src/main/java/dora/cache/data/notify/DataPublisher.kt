package dora.cache.data.notify

import androidx.lifecycle.MutableLiveData

abstract class DataPublisher<M> : IDataPublisher<M> {

    private var subscriber: IDataSubscriber<M>? = null
    protected val liveDataMap = HashMap<Class<*>, MutableLiveData<Any>>()

    override fun setSubscriber(subscriber: IDataSubscriber<M>) {
        this.subscriber = subscriber
    }

    override fun <T> send(modelType: Class<T>, data: T?) {
        subscriber?.relay(modelType, this)
    }

    override fun getLiveData(modelType: Class<*>): MutableLiveData<*>? {
        return liveDataMap[modelType]
    }

    override fun getLastValue(modelType: Class<*>): Any? {
        return getLiveData(modelType)?.value
    }

    override fun clearLastValue(modelType: Class<*>) {
        getLiveData(modelType)?.postValue(null)
    }
}