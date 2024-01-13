package dora.cache.data.visitor

import kotlin.random.Random

/**
 * 从样本数据中随机读取数据的数据分页器，不保证去重。
 */
class RandomPageDataVisitor<M> : BasePageDataVisitor<M>() {

    override fun filterPageData(models: MutableList<M>, totalCount: Int, currentPage: Int, pageSize: Int): MutableList<M> {
        val result: MutableList<M> = arrayListOf()
        val pageCount = if (totalCount % pageSize == 0) totalCount / pageSize else totalCount / pageSize + 1
        for (i in 0 until pageCount) {
            result.add(models[Random.nextInt(totalCount)])
        }
        return result
    }
}