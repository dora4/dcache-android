package dora.cache.data.notify

import androidx.lifecycle.MutableLiveData

abstract class ListDataPublisher<M> : IListDataPublisher<M> {

    private var subscriber: IListDataSubscriber<M>? = null
    protected val liveDataMap = HashMap<Class<*>, MutableLiveData<MutableList<*>>>()

    override fun setSubscriber(subscriber: IListDataSubscriber<M>) {
        this.subscriber = subscriber
    }

    override fun <T> send(modelType: Class<T>, data: MutableList<T>) {
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