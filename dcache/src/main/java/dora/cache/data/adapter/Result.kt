package dora.cache.data.adapter

/**
 * If the model returned by the REST API interface is not directly the model object to be cached
 * (i.e., not a subclass of [dora.db.table.OrmTable]), use this interface to specify the data to
 * be cached. If the root node of the data is to be cached, this is not required.
 * 简体中文：如果REST API接口返回的model并非直接为要缓存的model对象（即非[dora.db.table.OrmTable]的子类），则
 * 通过这个接口指定要缓存的数据，如果将其数据根结点缓存，则无需使用它。
 *
 * @see ResultAdapter
 */
interface Result<M> {

    /**
     * Specify the actual model to be cached, such as a certain attribute of the data returned by
     * the REST API, like the data field.
     * 简体中文：指定真实要缓存的model，如返回REST API接口返回数据的某个属性，如data字段。
     */
    fun getRealModel() : M?
}