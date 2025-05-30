package dora.cache.data.notify

import androidx.annotation.CallSuper
import androidx.lifecycle.MutableLiveData

/**
 * Data publisher.
 * 简体中文：数据发布者。
 */
interface IDataPublisher<M> {

    /**
     * Set data subscriber.
     * 简体中文：设置数据订阅者。
     */
    fun setSubscriber(subscriber: IDataSubscriber<M>)

    /**
     * Send data to all subscribers except itself.
     * 简体中文：发送数据给除自己以外的所有的订阅者。
     */
    @CallSuper
    fun <T> send(modelType: Class<T>, data: T?)

    /**
     * Receive updated data from the publisher.
     * 简体中文：接收发布者更新的数据。
     */
    fun receive(isDeterminate: Boolean, modelType: Class<*>, liveData: MutableLiveData<*>)

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

    /**
     * Used to clear the last published value of the default publisher.
     * 简体中文：用于清除默认发布者最后发布的值。
     */
    fun clearLastValue(modelType: Class<*>)

    companion object {

        @JvmStatic
        val DEFAULT = object : DataPublisher<Any>() {

            override fun <T> send(modelType: Class<T>, data: T?) {
                if (liveDataMap.contains(modelType)) {
                    liveDataMap[modelType]?.postValue(data)
                } else {
                    val liveData = MutableLiveData<Any>()
                    liveData.postValue(data as Any)
                    liveDataMap[modelType] = liveData
                }
                super.send(modelType, data)
            }

            override fun receive(
                isDeterminate: Boolean,
                modelType: Class<*>,
                liveData: MutableLiveData<*>
            ) {
            }
        }
    }
}