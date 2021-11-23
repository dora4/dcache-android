package dora.cache.data.model

import java.text.SimpleDateFormat
import kotlin.properties.Delegates

/**
 * 推荐块存储使用这个model。
 */
open class BlockModel : Comparable<BlockModel> {

    /**
     * 记录数据保存时的时间戳。
     */
    var timestamp by Delegates.notNull<Long>()

    /**
     * 判断数据的内容是否相同，不区分保存时间。
     */
    lateinit var hash: String

    override fun compareTo(other: BlockModel): Int {
        return this.timestamp.compareTo(other.timestamp)
    }

    fun before(timestamp: Long) : Boolean {
        return this.timestamp < timestamp
    }

    fun after(timestamp: Long) : Boolean {
        return this.timestamp > timestamp
    }

    fun during(start: Long, end: Long) : Boolean {
        return this.timestamp in start..end
    }

    override fun equals(other: Any?): Boolean {
        other?.let {
            if (it is BlockModel) {
                return this.hash == it.hash
            }
        }
        return false
    }

    /**
     * 方便查看数据的存储时间。
     */
    fun getTime() : String {
        return getTime("yyyy-MM-dd HH:mm:ss")
    }

    fun getTime(formatter: String) : String {
        return SimpleDateFormat(formatter).format(this.timestamp)
    }
}