package dora.cache.data.notify

import androidx.lifecycle.MutableLiveData
import java.lang.reflect.ParameterizedType
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

    private fun getGenericType(obj: Any): Class<*> {
        return if (obj.javaClass.genericSuperclass is ParameterizedType &&
            (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments.isNotEmpty()) {
            (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>
        } else (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>
    }

    override fun unsubscribe(publisher: IListDataPublisher<M>) {
        if (publisher != IListDataPublisher.DEFAULT) {
            publisher.receive(getGenericType(this), MutableLiveData())
            publishers.remove(publisher)
        }
    }

    override fun relay(modelType: Class<*>, publisher: IListDataPublisher<M>) {
        for (pub in publishers) {
            synchronized(this) {
                if ((pub == publisher && pub == IListDataPublisher.DEFAULT)
                    || pub != publisher) {
                    val liveData = publisher.getListLiveData(modelType)
                    liveData?.let {
                        pub.receive(modelType, it)
                    }
                }
            }
        }
    }

    companion object {

        private val instances = mutableMapOf<Class<*>, ListDataSubscriber<*>>()

        @JvmStatic
        fun <M> getInstance(clazz: Class<M>): ListDataSubscriber<M> {
            return instances.getOrPut(clazz) {
                ListDataSubscriber<M>()
            } as ListDataSubscriber<M>
        }
    }
}