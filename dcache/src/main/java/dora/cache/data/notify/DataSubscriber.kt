package dora.cache.data.notify

import androidx.lifecycle.MutableLiveData
import java.lang.reflect.ParameterizedType
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

    private fun getGenericType(obj: Any): Class<*> {
        return if (obj.javaClass.genericSuperclass is ParameterizedType &&
            (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments.isNotEmpty()) {
            (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>
        } else (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>
    }

    override fun unsubscribe(publisher: IDataPublisher<M>) {
        if (publisher != IDataPublisher.DEFAULT) {
            publisher.receive(true, getGenericType(this), MutableLiveData<M>())
            publishers.remove(publisher)
        }
    }

    override fun relay(modelType: Class<*>, publisher: IDataPublisher<M>) {
        for (pub in publishers) {
            synchronized(this) {
                if ((pub == publisher && pub == IListDataPublisher.DEFAULT)
                    || pub != publisher) {
                    val liveData = publisher.getLiveData(modelType)
                    liveData?.let {
                        pub.receive(false, modelType, it)
                    }
                }
            }
        }
    }

    companion object {

        private val instances = mutableMapOf<Class<*>, DataSubscriber<*>>()

        @JvmStatic
        fun <M> getInstance(clazz: Class<M>): DataSubscriber<M> {
            return instances.getOrPut(clazz) {
                DataSubscriber<M>()
            } as DataSubscriber<M>
        }
    }
}