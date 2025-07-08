package dora.db.builder

/**
 * Query condition builder, used for conveniently specifying query conditions.
 * 简体中文：查询条件的构建者，用于方便指定查询条件。
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
     * Specify the WHERE clause.
     * 简体中文：指定where子句。
     */
    fun where(builder: WhereBuilder): QueryBuilder {
        whereBuilder = builder
        return this
    }

    /**
     * Specify the WHERE clause.
     * 简体中文：指定where子句。
     */
    fun where(condition: Condition): QueryBuilder {
        whereBuilder = WhereBuilder.create()
        whereBuilder.where(condition)
        return this
    }

    /**
     * Specify the columns to be queried.
     * 简体中文：指定要查询的列。
     */
    fun column(columns: Array<String>): QueryBuilder {
        this.columns = columns
        return this
    }

    /**
     * Specify the HAVING clause.
     * 简体中文：指定having子句。
     */
    fun having(having: String): QueryBuilder {
        this.having = HAVING + having
        return this
    }

    /**
     * Specify the ORDER BY clause.
     * 简体中文：指定order by子句。
     */
    fun orderBy(order: String): QueryBuilder {
        this.order = ORDER_BY + order
        return this
    }

    /**
     * Specify the GROUP BY clause.
     * 简体中文：指定group by子句。
     */
    fun groupBy(group: String): QueryBuilder {
        this.group = GROUP_BY + group
        return this
    }

    /**
     * Specify the LIMIT clause.
     * 简体中文：指定limit子句。
     */
    fun limit(limit: Int): QueryBuilder {
        this.limit = LIMIT + limit
        return this
    }

    /**
     * Specify the LIMIT clause, starting the data index from [start] and retrieving [size] records.
     * 简体中文：指定limit子句，数据索引从[start]开始，取[size]条数据。
     */
    fun limit(start: Int, size: Int): QueryBuilder {
        limit = LIMIT + start + COMMA + size
        return this
    }

    /**
     * Build the SQL statement for the query.
     * 简体中文：构建query的sql语句。
     */
    fun build(): String {
        return (whereBuilder.build() + (if (group != null) group else SPACE) + (if (having != null)
            having else SPACE) + (if (order != null) order else SPACE) + if (limit != null) limit
        else SPACE)
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
     * Convert to the [Condition] class.
     * 简体中文：转化为[Condition]类。
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

        @JvmStatic
        fun create(): QueryBuilder {
            return QueryBuilder()
        }

        @JvmStatic
        fun create(condition: Condition): QueryBuilder {
            val query = create()
            query.whereBuilder = WhereBuilder.create(condition)
            query.limit = condition.limit
            query.order = condition.orderBy
            query.group = condition.groupBy
            query.having = condition.having
            return query
        }
    }

    init {
        whereBuilder = WhereBuilder.create()
    }
}