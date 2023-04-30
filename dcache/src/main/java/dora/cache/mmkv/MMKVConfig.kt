package dora.cache.mmkv

import android.content.Context
import android.text.TextUtils
import com.tencent.mmkv.MMKV

object MMKVConfig {

    private var builder: Builder = Builder()

    fun getBuilder() : Builder {
        return builder
    }

    fun initConfig(block: Builder.() -> Unit) {
        block
    }

    class Builder {

        private var path: String? = null

        fun path(path: String) {
            this.path = path
        }

        fun build(context: Context) : String {
            if (!TextUtils.isEmpty(path)) {
                return MMKV.initialize(context, path)
            }
            return MMKV.initialize(context)
        }
    }
}