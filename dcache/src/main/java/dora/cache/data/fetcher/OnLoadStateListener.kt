package dora.cache.data.fetcher

/**
 * An interface for listening to the success or failure status of calls to the fetchData() or
 * fetchListData() methods of [dora.cache.repository.BaseRepository].
 * 简体中文：如调用[dora.cache.repository.BaseRepository]的fetchData()或fetchListData()方法成功或失败的状
 * 态的监听接口。
 */
interface OnLoadStateListener {

    companion object {

        /**
         * Data loaded successfully.
         * 简体中文：加载数据成功。
         */
        const val SUCCESS = 0

        /**
         * Failed to load data.
         * 简体中文：加载数据失败。
         */
        const val FAILURE = 1
    }

    enum class Source {
        CACHE,
        NETWORK,
        OTHER
    }

    /**
     * 0 represents success, and 1 represents failure.
     * 简体中文：0代表成功，1代表失败。
     */
    fun onLoad(from: Source, state: Int)
}