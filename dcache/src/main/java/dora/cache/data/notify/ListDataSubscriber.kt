package dora.cache.data.notify

import java.util.concurrent.CopyOnWriteArrayList

class ListDataSubscriber<M> private constructor() : IListDataSubscriber<M> {

    private val publishers = CopyOnWriteArrayList<IListDataPublisher<M>>()

    init {
        subscribe(IListDataPublisher.DEFAULT as IListDataPublisher<M>)
    }

    override fun subscribe(publisher: IListDataPublisher<M>) {
        synchronized(this) {
            if (!publishers.contains(publisher)) {
                publishers.add(publisher)
                publisher.setSubscriber(this)
            }
        }
    }

    override fun relay(type: String, publisher: IListDataPublisher<M>) {
        for (pub in publishers) {
            synchronized(this) {
                if (pub != publisher) {
                    pub.receive(type, publisher.getListLiveData())
                }
            }
        }
    }

    companion object {

        private val instances = mutableMapOf<Class<*>, ListDataSubscriber<*>>()

        @JvmStatic
        fun <M> getInstance(clazz: Class<M>): ListDataSubscriber<M> {
            return synchronized(this) {
                val instance = instances[clazz]
                if (instance is ListDataSubscriber<*>) {
                    @Suppress("UNCHECKED_CAST")
                    return instance as ListDataSubscriber<M>
                }
                val newInstance = ListDataSubscriber<M>()
                instances[clazz] = newInstance
                newInstance
            }
        }
    }
}