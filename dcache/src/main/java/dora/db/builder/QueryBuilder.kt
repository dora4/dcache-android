package dora.db.builder

/**
 * 查询条件构建者。
 *
 * @see WhereBuilder
 */
class QueryBuilder private constructor() {

    private var columns: Array<String>? = null
    private var group: String? = null
    private var having: String? = null
    private var order: String? = null
    private var limit: String? = null
    private var whereBuilder: WhereBuilder

    /**
     * 指定where子句。
     */
    fun where(builder: WhereBuilder): QueryBuilder {
        whereBuilder = builder
        return this
    }

    /**
     * 指定where子句。
     */
    fun where(condition: Condition): QueryBuilder {
        whereBuilder = WhereBuilder.create()
        whereBuilder.where(condition)
        return this
    }

    /**
     * 指定要查询的列。
     */
    fun column(columns: Array<String>): QueryBuilder {
        this.columns = columns
        return this
    }

    /**
     * 指定having子句。
     */
    fun having(having: String): QueryBuilder {
        this.having = HAVING + having
        return this
    }

    /**
     * 指定order by。
     */
    fun orderBy(order: String): QueryBuilder {
        this.order = ORDER_BY + order
        return this
    }

    /**
     * 指定group by。
     */
    fun groupBy(group: String): QueryBuilder {
        this.group = GROUP_BY + group
        return this
    }

    /**
     * 指定limit。
     */
    fun limit(limit: Int): QueryBuilder {
        this.limit = LIMIT + limit
        return this
    }

    /**
     * 指定limit，数据索引从start开始，数size条数据。
     */
    fun limit(start: Int, size: Int): QueryBuilder {
        limit = LIMIT + start + COMMA + size
        return this
    }

    /**
     * 构建query的sql语句。
     */
    fun build(): String {
        return (whereBuilder.build() + (if (group != null) group else SPACE) + (if (having != null) having else SPACE)
                + (if (order != null) order else SPACE) + if (limit != null) limit else SPACE)
    }

    fun getColumns() : Array<String>? {
        return columns
    }

    fun getWhereBuilder(): WhereBuilder {
        return whereBuilder
    }

    fun getHaving(): String {
        return if (having != null) StringBuilder(having).delete(0, HAVING.length).toString() else SPACE
    }

    fun getOrder(): String {
        return if (order != null) StringBuilder(order).delete(0, ORDER_BY.length).toString() else SPACE
    }

    fun getGroup(): String {
        return if (group != null) StringBuilder(group).delete(0, GROUP_BY.length).toString() else SPACE
    }

    fun getLimit(): String {
        return if (limit != null) StringBuilder(limit).delete(0, LIMIT.length).toString() else SPACE
    }

    /**
     * 转化为Condition。
     */
    fun toCondition(): Condition {
        return Condition(whereBuilder.selection, whereBuilder.selectionArgs ?: arrayOf(),
                limit, order, group, having)
    }

    companion object {

        private const val GROUP_BY = " GROUP BY "
        private const val HAVING = " HAVING "
        private const val ORDER_BY = " ORDER BY "
        private const val LIMIT = " LIMIT "
        private const val COMMA = ","
        private const val SPACE = ""

        fun create(): QueryBuilder {
            return QueryBuilder()
        }
    }

    init {
        whereBuilder = WhereBuilder.create()
    }
}