package dora.cache

import java.util.*

class LruCache<K, V>(private val initialMaxSize: Int) : Cache<K, V> {

    private val cache = LinkedHashMap<K, V?>(100, 0.75f, true)

    private var currentSize = 0

    /**
     * 返回当前缓存所能允许的最大 size
     *
     * @return `maxSize`
     */
    var maxSize: Int

    /**
     * 设置一个系数应用于当时构造函数中所传入的 size，从而得到一个新的 [maxSize]，
     * 并会立即调用 [evict] 开始清除满足条件的条目。
     *
     * @param multiplier 系数
     */
    @Synchronized
    fun setSizeMultiplier(multiplier: Float) {
        require(multiplier >= 0) { "Multiplier must be >= 0" }
        maxSize = Math.round(initialMaxSize * multiplier)
        evict()
    }

    /**
     * 返回每个 `item` 所占用的 size，默认为1,这个 size 的单位必须和构造函数所传入的 size 一致，
     * 子类可以重写这个方法以适应不同的单位，比如说 bytes。
     *
     * @param item 每个 `item` 所占用的 size
     * @return 单个 item 的 `size`
     */
    protected fun getItemSize(item: V?): Int {
        return 1
    }

    /**
     * 当缓存中有被驱逐的条目时，会回调此方法，默认空实现，子类可以重写这个方法。
     *
     * @param key   被驱逐条目的 `key`
     * @param value 被驱逐条目的 `value`
     */
    protected fun onItemEvicted(key: K, value: V?) {
        // optional override
    }

    /**
     * 返回当前缓存已占用的总 size。
     *
     * @return `size`
     */
    @Synchronized
    override fun size(): Int {
        return currentSize
    }

    /**
     * 如果这个 `key` 在缓存中有对应的 `value` 并且不为 `null`，则返回 true。
     *
     * @param key 用来映射的 `key`
     * @return `true` 为在容器中含有这个 `key`, 否则为 `false`
     */
    @Synchronized
    override fun containsKey(key: K): Boolean {
        return cache.containsKey(key)
    }

    /**
     * 返回当前缓存中含有的所有 `key`。
     *
     * @return `keySet`
     */
    @Synchronized
    override fun keySet(): Set<K> {
        return cache.keys
    }

    /**
     * 返回这个 `key` 在缓存中对应的 `value`，如果返回 `null` 说明这个 `key` 没有对应的 `value`。
     *
     * @param key 用来映射的 `key`
     * @return `value`
     */
    @Synchronized
    override fun get(key: K): V? {
        return cache[key]
    }

    /**
     * 将 `key` 和 `value` 以条目的形式加入缓存，如果这个 `key` 在缓存中已经有对应的 `value`，
     * 则此 `value` 被新的 `value` 替换并返回，如果为 `null` 说明是一个新条目。
     *
     *
     * 如果 [getItemSize] 返回的 size 大于或等于缓存所能允许的最大 size，则不能向缓存中添加此条目，
     * 此时会回调 [onItemEvicted] 通知此方法当前被驱逐的条目。
     *
     * @param key   通过这个 `key` 添加条目
     * @param value 需要添加的 `value`
     * @return 如果这个 `key` 在容器中已经储存有 `value`, 则返回之前的 `value` 否则返回 `null`
     */
    @Synchronized
    override fun put(key: K, value: V): V? {
        val itemSize = getItemSize(value)
        if (itemSize >= maxSize) {
            onItemEvicted(key, value)
            return null
        }
        val result = cache.put(key, value)
        if (value != null) {
            currentSize += getItemSize(value)
        }
        if (result != null) {
            currentSize -= getItemSize(result)
        }
        evict()
        return result
    }

    /**
     * 移除缓存中这个 `key` 所对应的条目，并返回所移除条目的 `value`。
     * 如果返回为 `null` 则有可能时因为这个 `key` 对应的 `value` 为 `null` 或条目不存在。
     *
     * @param key 使用这个 `key` 移除对应的条目
     * @return 如果这个 `key` 在容器中已经储存有 `value` 并且删除成功则返回删除的 `value`, 否则返回 `null`
     */
    @Synchronized
    override fun remove(key: K): V? {
        val value = cache.remove(key)
        if (value != null) {
            currentSize -= getItemSize(value)
        }
        return value
    }

    /**
     * 清除缓存中所有的内容。
     */
    override fun clear() {
        trimToSize(0)
    }

    /**
     * 当指定的 size 小于当前缓存已占用的总 size 时，会开始清除缓存中最近最少使用的条目。
     *
     * @param size `size`
     */
    @Synchronized
    protected fun trimToSize(size: Int) {
        var last: Map.Entry<K, V?>
        while (currentSize > size) {
            last = cache.entries.iterator().next()
            val toRemove = last.value
            currentSize -= getItemSize(toRemove)
            val key = last.key
            cache.remove(key)
            onItemEvicted(key, toRemove)
        }
    }

    /**
     * 当缓存中已占用的总 size 大于所能允许的最大 size，会使用  [trimToSize] 开始清除满足条件的条目。
     */
    private fun evict() {
        trimToSize(maxSize)
    }

    /**
     * Constructor for LruCache.
     *
     * @param size 这个缓存的最大 size，这个 size 所使用的单位必须和 [getItemSize] 所使用的单位一致。
     */
    init {
        maxSize = initialMaxSize
    }
}