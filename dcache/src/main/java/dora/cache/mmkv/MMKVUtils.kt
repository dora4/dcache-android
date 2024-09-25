package dora.cache.mmkv

import com.google.gson.Gson
import com.tencent.mmkv.MMKV

/**
 * Before using Kotlin, please initialize by calling MMKVConfig.initConfig{} or use
 * MMKVConfig.getBuilder() in Java. Simple data, such as global configuration caches, can utilize
 * MMKV without the need for a Repository. The data stored in MMKV does not involve data filtering,
 * and there should not be multiple entries of the same type of data.
 * 简体中文：使用前kotlin请先调用MMKVConfig.initConfig{}或java调用MMKVConfig.getBuilder()进行初始化。简单的
 * 数据如全局的配置的缓存可以使用MMKV，而无需使用Repository，MMKV保存的数据不会涉及到数据的过滤，同一类的数据也不应
 * 该有多个。
 */
object MMKVUtils {

    private val mmkv = MMKV.defaultMMKV()

    fun writeString(key: String, value: String) {
        mmkv.encode(key, value)
    }

    fun readString(key: String, defValue: String): String? {
        return mmkv.decodeString(key, defValue)
    }

    fun writeInteger(key: String, value: Int) {
        mmkv.encode(key, value)
    }

    fun readInteger(key: String, defValue: Int): Int {
        return mmkv.decodeInt(key, defValue)
    }

    fun writeBoolean(key: String, value: Boolean) {
        mmkv.encode(key, value)
    }

    fun readBoolean(key: String, defValue: Boolean): Boolean {
        return mmkv.decodeBool(key, defValue)
    }

    fun <T> writeObject(key: String, value: T, clazz: Class<T>) {
        val objJson = Gson().toJson(value, clazz)
        writeString(key, objJson)
    }

    fun <T> readObject(key: String, clazz: Class<T>) : T? {
        val objJson = readString(key, "") ?: return null
        return Gson().fromJson(objJson, clazz)
    }

    fun remove(key: String) {
        mmkv.removeValueForKey(key)
    }

    fun clear() {
        mmkv.clear()
    }
}