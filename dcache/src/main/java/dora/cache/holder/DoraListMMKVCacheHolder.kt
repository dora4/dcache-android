package dora.cache.holder;

import android.content.Context
import com.google.gson.reflect.TypeToken
import dora.cache.mmkv.MMKVConfig
import dora.cache.mmkv.MMKVUtils

class DoraListMMKVCacheHolder<M>(private val context: Context,
                                 private val path: String,
                                 private val typeToken: TypeToken<MutableList<M>>)
    : ListMMKVCacheHolder<M>() {

    constructor(context: Context,
                typeToken: TypeToken<MutableList<M>>) : this(context, "", typeToken)

    override fun init() {
        MMKVConfig.initConfig(context) {
            path(path)
        }
    }

    override fun removeOldCache(key: String) {
        MMKVUtils.remove(key)
    }

    override fun readCache(key: String): MutableList<M>? {
        return MMKVUtils.readListObject(key, typeToken.type)
    }

    override fun addNewCache(key: String, model: MutableList<M>) {
        MMKVUtils.writeObject(key, model, typeToken.rawType)
    }
}
