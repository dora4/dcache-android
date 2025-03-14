package dora.cache.data.notify

import java.util.concurrent.CopyOnWriteArrayList

class DataSubscriber<M> private constructor() : IDataSubscriber<M> {

    private val publishers = CopyOnWriteArrayList<IDataPublisher<M>>()

    init {
        subscribe(IDataPublisher.DEFAULT as IDataPublisher<M>)
    }

    override fun subscribe(publisher: IDataPublisher<M>) {
        synchronized(this) {
            if (!publishers.contains(publisher)) {
                publishers.add(publisher)
                publisher.setSubscriber(this)
            }
        }
    }

    override fun relay(modelType: Class<*>, publisher: IDataPublisher<M>) {
        for (pub in publishers) {
            synchronized(this) {
                if ((pub == publisher && pub == IListDataPublisher.DEFAULT)
                    || pub != publisher) {
                    val liveData = publisher.getLiveData(modelType)
                    liveData?.let {
                        pub.receive(modelType, it)
                    }
                }
            }
        }
    }

    companion object {

        private val instances = mutableMapOf<Class<*>, DataSubscriber<*>>()

        @JvmStatic
        fun <M> getInstance(clazz: Class<M>): DataSubscriber<M> {
            return synchronized(this) {
                val instance = instances[clazz]
                if (instance is DataSubscriber<*>) {
                    @Suppress("UNCHECKED_CAST")
                    return instance as DataSubscriber<M>
                }
                val newInstance = DataSubscriber<M>()
                instances[clazz] = newInstance
                newInstance
            }
        }
    }
}