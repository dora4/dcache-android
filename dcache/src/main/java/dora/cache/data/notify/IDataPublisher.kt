package dora.cache.data.notify

import androidx.annotation.CallSuper
import androidx.lifecycle.MutableLiveData

/**
 * Data Publisher.
 * 简体中文：数据发布者。
 */
interface IDataPublisher<M> {

    /**
     * Set data subscriber.
     * 简体中文：设置数据订阅者。
     */
    fun setSubscriber(subscriber: IDataSubscriber<M>)

    /**
     * Send data to all subscribers except itself, with the DEFAULT publisher unaffected.
     * 简体中文：发送数据给除自己以外的所有的订阅者，DEFAULT发布者不受影响。
     */
    @CallSuper
    fun <T> send(modelType: Class<T>, data: T?)

    /**
     * Receive updated data from the publisher.
     * 简体中文：接收发布者更新的数据。
     */
    fun receive(modelType: Class<*>, liveData: MutableLiveData<*>)

    /**
     * Get the [androidx.lifecycle.LiveData] object.
     * 简体中文：获取[androidx.lifecycle.LiveData]对象。
     */
    fun getLiveData(modelType: Class<*>) : MutableLiveData<*>?

    /**
     * Used to get the last published value of the default publisher.
     * 简体中文：用于获取默认发布者最后发布的值。
     */
    fun getLastValue(modelType: Class<*>) : Any?

    companion object {

        @JvmStatic
        val DEFAULT = object : DataPublisher<Any>() {

            override fun receive(modelType: Class<*>, liveData: MutableLiveData<*>) {
                liveDataMap[modelType] = liveData as MutableLiveData<Any>
            }
        }
    }
}