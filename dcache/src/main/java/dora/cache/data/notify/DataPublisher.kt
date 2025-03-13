package dora.cache.data.notify

import androidx.lifecycle.MutableLiveData

abstract class DataPublisher<M> : IDataPublisher<M> {

    private var subscriber: IDataSubscriber<M>? = null
    private val liveData: MutableLiveData<M?> = MutableLiveData()

    override fun setSubscriber(subscriber: IDataSubscriber<M>) {
        this.subscriber = subscriber
    }

    override fun send(type: String, data: M?) {
        liveData.postValue(data)
        subscriber?.relay(type, this)
    }

    override fun getLiveData(): MutableLiveData<M?> {
        return liveData
    }
}