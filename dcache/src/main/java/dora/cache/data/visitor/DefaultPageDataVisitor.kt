package dora.cache.data.visitor

import java.util.*

class DefaultPageDataVisitor<M> : BasePageDataVisitor<M>() {
    override fun getResult(model: List<M>, totalCount: Int, currentPage: Int, pageSize: Int): List<M> {
        val result: MutableList<M> = ArrayList()
        val pageCount = if (totalCount % pageSize == 0) totalCount / pageSize else totalCount / pageSize + 1
        for (i in 0 until pageCount) {
            result.add(data[currentPage * pageSize + i])
        }
        return result
    }
}