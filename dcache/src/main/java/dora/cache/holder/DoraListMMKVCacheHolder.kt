package dora.cache.holder;

import android.content.Context
import dora.cache.mmkv.MMKVConfig
import dora.cache.mmkv.MMKVUtils

class DoraListMMKVCacheHolder<M>(private val context: Context,
                                 private val path: String,
                                 private val clazz: Class<MutableList<M>>)
    : ListMMKVCacheHolder<M>() {

    constructor(context: Context,
                clazz: Class<MutableList<M>>) : this(context, "", clazz)

    override fun init() {
        MMKVConfig.initConfig(context) {
            path(path)
        }
    }

    override fun removeOldCache(key: String) {
        MMKVUtils.remove(key)
    }

    override fun readCache(key: String): MutableList<M>? {
        return MMKVUtils.readObject(key, clazz)
    }

    override fun addNewCache(key: String, model: MutableList<M>) {
        MMKVUtils.writeObject(key, model, clazz)
    }
}
