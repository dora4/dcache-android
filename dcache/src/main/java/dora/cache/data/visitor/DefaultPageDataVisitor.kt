package dora.cache.data.visitor

/**
 * The default data pagination.
 * 简体中文：默认的数据分页器。
 */
class DefaultPageDataVisitor<M> : BasePageDataVisitor<M>() {

    override fun filterPageData(models: MutableList<M>, totalCount: Int,
                                currentPage: Int, pageSize: Int): MutableList<M> {
        val result: MutableList<M> = arrayListOf()
        val pageCount = if (totalCount % pageSize == 0) totalCount / pageSize
            else totalCount / pageSize + 1
        for (i in 0 until pageCount) {
            result.add(models[currentPage * pageSize + i])
        }
        return result
    }
}