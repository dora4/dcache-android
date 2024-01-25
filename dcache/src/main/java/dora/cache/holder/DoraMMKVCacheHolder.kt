package dora.cache.holder;

import dora.cache.mmkv.MMKVConfig
import dora.cache.mmkv.MMKVUtils

class DoraMMKVCacheHolder<M>(val path: String, val clazz: Class<M>) : MMKVCacheHolder<M> {

    constructor(clazz: Class<M>) : this("", clazz)

    override fun init() {
        MMKVConfig.initConfig {
            path(path)
        }
    }

    override fun removeOldCache(key: String) {
        MMKVUtils.remove(key)
    }

    override fun readCache(key: String): M {
        return MMKVUtils.readObject(key, clazz) as M
    }

    override fun addNewCache(key: String, model: M) {
        MMKVUtils.writeObject(key, model, clazz)
    }
}
