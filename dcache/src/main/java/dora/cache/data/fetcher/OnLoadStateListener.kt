package dora.cache.data.fetcher

/**
 * 如调用[dora.cache.repository.BaseRepository]的fetchData()或fetchListData()方法成功或失败的状态的监听接
 * 口。
 */
interface OnLoadStateListener {

    companion object {

        /**
         * 加载数据成功。
         */
        const val SUCCESS = 0

        /**
         * 加载数据失败。
         */
        const val FAILURE = 1
    }

    /**
     * 0代表成功，1代表失败。
     */
    fun onLoad(state: Int)
}