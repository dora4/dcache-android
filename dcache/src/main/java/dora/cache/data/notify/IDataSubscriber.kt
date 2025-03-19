package dora.cache.data.notify

/**
 * Data subscriber, used to subscribe to global memory data updates.
 * 简体中文：数据订阅者，用于订阅全局的内存数据更新。
 */
interface IDataSubscriber<M> {

    /**
     * Subscribe to the data publisher.
     * 简体中文：订阅数据发布者。
     */
    fun subscribe(publisher: IDataPublisher<M>)

    /**
     * Unsubscribe to the data publisher.
     * 简体中文：取消订阅数据发布者。
     */
    fun unsubscribe(publisher: IDataPublisher<M>)

    /**
     * Forward data.
     * 简体中文：转发数据。
     */
    fun relay(modelType: Class<*>, publisher: IDataPublisher<M>)
}