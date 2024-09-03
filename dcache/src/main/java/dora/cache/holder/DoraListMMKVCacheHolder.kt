package dora.cache.holder;

import dora.cache.mmkv.MMKVConfig
import dora.cache.mmkv.MMKVUtils

class DoraListMMKVCacheHolder<M>(private val path: String, val clazz: Class<MutableList<M>>)
    : ListMMKVCacheHolder<M>() {

    constructor(clazz: Class<MutableList<M>>) : this("", clazz)

    override fun init() {
        MMKVConfig.initConfig {
            path(path)
        }
    }

    override fun removeOldCache(key: String) {
        MMKVUtils.remove(key)
    }

    override fun readCache(key: String): MutableList<M>? {
        return MMKVUtils.readObject(key, clazz) as MutableList<M>
    }

    override fun addNewCache(key: String, model: MutableList<M>) {
        MMKVUtils.writeObject(key, model, clazz)
    }
}
