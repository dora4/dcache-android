package dora.cache.data.visitor

import java.util.*

class DefaultPageDataVisitor<T> : BasePageDataVisitor<T>() {
    override fun getResult(data: List<T>, totalCount: Int, currentPage: Int, pageSize: Int): List<T> {
        val result: MutableList<T> = ArrayList()
        val pageCount = if (totalCount % pageSize == 0) totalCount / pageSize else totalCount / pageSize + 1
        for (i in 0 until pageCount) {
            result.add(data[currentPage * pageSize + i])
        }
        return result
    }
}