package dora.cache.data.adapter

/**
 * 如果api接口返回的model并非直接为要缓存的model对象，即非[dora.db.table.OrmTable]的子类，则通过这个接口指定要
 * 缓存的数据。
 */
interface Result<M> {

    /**
     * 真实要缓存的model，返回api接口返回数据的一个属性。
     */
    fun getRealModel() : M?
}