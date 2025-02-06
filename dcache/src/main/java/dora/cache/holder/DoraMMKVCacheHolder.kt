package dora.cache.holder;

import android.content.Context
import dora.cache.mmkv.MMKVConfig
import dora.cache.mmkv.MMKVUtils

class DoraMMKVCacheHolder<M>(private val context: Context,
                             private val path: String,
                             private val clazz: Class<M>) : MMKVCacheHolder<M> {

    constructor(context: Context, clazz: Class<M>) : this(context, "", clazz)

    override fun init() {
        MMKVConfig.initConfig(context) {
            path(path)
        }
    }

    override fun removeOldCache(key: String) {
        MMKVUtils.remove(key)
    }

    override fun readCache(key: String): M? {
        return MMKVUtils.readObject(key, clazz)
    }

    override fun addNewCache(key: String, model: M) {
        MMKVUtils.writeObject(key, model, clazz)
    }
}
