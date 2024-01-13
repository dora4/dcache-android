package dora.cache.data.adapter

/**
 * 如果REST API接口返回的model并非直接为要缓存的model对象（即非[dora.db.table.OrmTable]的子类），则通过这个
 * 接口指定要缓存的数据，如果将其数据根结点缓存，则无需使用它。
 *
 * @see ResultAdapter
 */
interface Result<M> {

    /**
     * 指定真实要缓存的model，如返回REST API接口返回数据的某个属性，如data字段。
     */
    fun getRealModel() : M?
}