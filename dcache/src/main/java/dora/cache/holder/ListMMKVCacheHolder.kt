package dora.cache.holder

/**
 * mmkv缓存holder，方便用于列表数据。
 */
abstract class ListMMKVCacheHolder<M> : MMKVCacheHolder<MutableList<M>>