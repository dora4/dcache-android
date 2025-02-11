package dora.cache.data.fetcher

/**
 * The default implementation of the data loading status listener, which does not handle the
 * business logic after success or failure.
 * 简体中文：加载数据状态监听器的默认实现，没有处理成功还是失败后的业务逻辑。
 */
class OnLoadListenerImpl : OnLoadListener {

    override fun onLoad(from: OnLoadListener.Source, state: Int) {
    }
}