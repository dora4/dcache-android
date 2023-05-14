package dora.cache.data.fetcher

interface OnLoadStateListener {

    companion object {
        const val SUCCESS = 0
        const val FAILURE = 1
    }

    /**
     * 0代表成功，1代表失败。
     */
    fun onLoad(state: Int)
}