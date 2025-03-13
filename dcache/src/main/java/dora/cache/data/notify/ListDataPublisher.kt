package dora.cache.data.notify

import androidx.lifecycle.MutableLiveData

abstract class ListDataPublisher<M> : IListDataPublisher<M> {

    private var subscriber: IListDataSubscriber<M>? = null
    private val liveData: MutableLiveData<MutableList<M>> = MutableLiveData()

    override fun setSubscriber(subscriber: IListDataSubscriber<M>) {
        this.subscriber = subscriber
    }

    override fun send(type: String, data: MutableList<M>) {
        liveData.postValue(data)
        subscriber?.relay(type, this)
    }

    override fun getListLiveData(): MutableLiveData<MutableList<M>> {
        return liveData
    }
}