package dora.cache

/**
 * Provides a deterministic key for comparing parameters.
 * Implementing this interface causes backpressure/deduplication to use the
 * comparisonKey() return value for comparison. Objects not implementing this
 * interface will use their toString() method for comparison.
 * 简体中文：为参数提供一个用于比较的、确定性的键；实现该接口后，背压/去重时会使用 comparisonKey() 返回的值进行比较。
 * 未实现该接口的对象则使用 toString() 方法进行比较。
 */
interface IRequestParam {

    /**
     * Returns a key for comparison. Must be deterministic and unique.
     * 简体中文：返回一个用于比较的键，必须是确定且唯一的。
     *
     * @since 3.5.0
     */
    fun comparisonKey(): Any
}
