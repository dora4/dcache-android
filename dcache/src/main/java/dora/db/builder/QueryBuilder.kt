package dora.db.builder

class QueryBuilder private constructor() {

    var columns: Array<String>? = null
    private var group: String? = null
    private var having: String? = null
    private var order: String? = null
    private var limit: String? = null
    private var whereBuilder: WhereBuilder

    fun where(builder: WhereBuilder): QueryBuilder {
        whereBuilder = builder
        return this
    }

    fun where(condition: Condition): QueryBuilder {
        whereBuilder = WhereBuilder.create()
        whereBuilder.where(condition)
        return this
    }

    fun column(columns: Array<String>): QueryBuilder {
        this.columns = columns
        return this
    }

    fun having(having: String): QueryBuilder {
        this.having = HAVING + having
        return this
    }

    fun orderBy(order: String): QueryBuilder {
        this.order = ORDER_BY + order
        return this
    }

    fun groupBy(group: String): QueryBuilder {
        this.group = GROUP_BY + group
        return this
    }

    fun limit(limit: Int): QueryBuilder {
        this.limit = LIMIT + limit
        return this
    }

    fun limit(start: Int, end: Int): QueryBuilder {
        limit = LIMIT + start + COMMA + end
        return this
    }

    fun build(): String {
        return (whereBuilder.build() + (if (group != null) group else SPACE) + (if (having != null) having else SPACE)
                + (if (order != null) order else SPACE) + if (limit != null) limit else SPACE)
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