package dora.cache.holder

/**
 * MMKV cache holder, convenient for list data.
 * 简体中文：mmkv缓存holder，方便用于列表数据。
 */
abstract class ListMMKVCacheHolder<M> : MMKVCacheHolder<MutableList<M>>