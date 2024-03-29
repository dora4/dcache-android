package dora.db.builder

/**
 * 查询条件where部分的构建者，即为简单查询条件，和QueryBuilder类似，最终都是得到Condition。
 *
 * @see QueryBuilder
 */
class WhereBuilder {

    var selection: String = ""
        private set
    var selectionArgs: Array<String?>? = null

    private constructor()

    private constructor(condition: Condition) {
        selection = condition.selection
        selectionArgs = condition.selectionArgs
    }

    private constructor(whereClause: String, whereArgs: Array<String?>?) {
        this.selection = whereClause
        this.selectionArgs = whereArgs
    }

    /**
     * 添加sql语句片段，and语句。
     */
    fun and(): WhereBuilder {
        selection += AND
        return this
    }

    /**
     * 添加sql语句片段，or语句。
     */
    fun or(): WhereBuilder {
        selection += OR
        return this
    }

    /**
     * 添加sql语句片段，not语句。
     */
    fun not(): WhereBuilder {
        selection += NOT
        return this
    }

    /**
     * 添加sql语句片段，and语句。
     */
    fun and(whereClause: String, vararg whereArgs: Any): WhereBuilder {
        return append(AND, whereClause, arrayOf(whereArgs))
    }

    /**
     * 添加sql语句片段，or语句。
     */
    fun or(whereClause: String, vararg whereArgs: Any): WhereBuilder {
        return append(OR, whereClause, arrayOf(whereArgs))
    }

    /**
     * 添加sql语句片段，not语句。
     */
    fun not(whereClause: String, vararg whereArgs: Any): WhereBuilder {
        return not().parenthesesLeft().append(null, whereClause, arrayOf(whereArgs)).parenthesesRight()
    }

    /**
     * 添加sql语句片段，and not语句。
     */
    fun andNot(whereClause: String, whereArgs: Array<Any>): WhereBuilder {
        return and(not(whereClause, whereArgs))
    }

    /**
     * 添加sql语句片段，or not语句。
     */
    fun orNot(whereClause: String, whereArgs: Array<Any>): WhereBuilder {
        return or(not(whereClause, whereArgs))
    }

    /**
     * 添加sql语句片段，and语句。
     */
    fun and(builder: WhereBuilder): WhereBuilder {
        val selection = builder.selection
        val selectionArgs = builder.selectionArgs
        return and(selection, selectionArgs!!)
    }

    /**
     * 添加sql语句片段，or语句。
     */
    fun or(builder: WhereBuilder): WhereBuilder {
        val selection = builder.selection
        val selectionArgs = builder.selectionArgs
        return or(selection, selectionArgs!!)
    }

    /**
     * 添加sql语句片段，not语句。
     */
    fun not(builder: WhereBuilder): WhereBuilder {
        val selection = builder.selection
        val selectionArgs = builder.selectionArgs
        return not(selection, selectionArgs!!)
    }

    /**
     * 添加sql语句片段，and not语句。
     */
    fun andNot(builder: WhereBuilder): WhereBuilder {
        return and(not(builder))
    }

    /**
     * 添加sql语句片段，or not语句。
     */
    fun orNot(builder: WhereBuilder): WhereBuilder {
        return or(not(builder))
    }

    /**
     * 添加sql语句片段，左括号"（"。
     */
    fun parenthesesLeft(): WhereBuilder {
        if (selection != null) {
            selection += PARENTHESES_LEFT
        } else {
            selection = PARENTHESES_LEFT
        }
        return this
    }

    /**
     * 添加sql语句片段，右括号"）"。
     */
    fun parenthesesRight(): WhereBuilder {
        if (selection != null) {
            selection += PARENTHESES_RIGHT
        }
        return this
    }

    /**
     * 添加sql语句片段，如a = 0。
     */
    fun addWhereEqualTo(column: String, value: Any): WhereBuilder {
        return append(null, column + EQUAL_HOLDER, arrayOf(value.toString()))
    }

    /**
     * 添加sql语句片段，如a != 0。
     */
    fun addWhereNotEqualTo(column: String, value: Any): WhereBuilder {
        return append(null, column + NOT_EQUAL_HOLDER, arrayOf(value.toString()))
    }

    /**
     * 添加sql语句片段，如a > 0。
     */
    fun addWhereGreaterThan(column: String, value: Number): WhereBuilder {
        return append(null, column + GREATER_THAN_HOLDER, arrayOf(value.toString()))
    }

    /**
     * 添加sql语句片段，如a >= 0。
     */
    fun addWhereGreaterThanOrEqualTo(column: String, value: Number): WhereBuilder {
        return append(null, column + GREATER_THAN_OR_EQUAL_TO_HOLDER,
            arrayOf(value.toString()))
    }

    /**
     * 添加sql语句片段，如a < 10。
     */
    fun addWhereLessThan(column: String, value: Number): WhereBuilder {
        return append(null, column + LESS_THAN_HOLDER, arrayOf(value.toString()))
    }

    /**
     * 添加sql语句片段，如a <= 10。
     */
    fun addWhereLessThanOrEqualTo(column: String, value: Number): WhereBuilder {
        return append(null, column + LESS_THAN_OR_EQUAL_TO_HOLDER,
            arrayOf(value.toString()))
    }

    /**
     * 添加sql语句片段，如a in(?,?,...)。
     */
    fun addWhereIn(column: String, values: Array<Any>): WhereBuilder {
        return appendWhereIn(null, column, values)
    }

    /**
     * 添加sql语句片段，如and a = 0。
     */
    fun andWhereEqualTo(column: String, value: Any): WhereBuilder {
        return append(AND, column + EQUAL_HOLDER, arrayOf(value.toString()))
    }

    /**
     * 添加sql语句片段，如and a != 0。
     */
    fun andWhereNotEqualTo(column: String, value: Any): WhereBuilder {
        return append(AND, column + NOT_EQUAL_HOLDER, arrayOf(value.toString()))
    }

    /**
     * 添加sql语句片段，如and a > 0。
     */
    fun andWhereGreaterThan(column: String, value: Number): WhereBuilder {
        return append(AND, column + GREATER_THAN_HOLDER, arrayOf(value.toString()))
    }

    /**
     * 添加sql语句片段，如and a >= 0。
     */
    fun andWhereGreaterThanOrEqualTo(column: String, value: Number): WhereBuilder {
        return append(AND, column + GREATER_THAN_OR_EQUAL_TO_HOLDER, arrayOf(value.toString()))
    }

    /**
     * 添加sql语句片段，如and a < 10。
     */
    fun andWhereLessThan(column: String, value: Number): WhereBuilder {
        return append(AND, column + LESS_THAN_HOLDER, arrayOf(value.toString()))
    }

    /**
     * 添加sql语句片段，如and a <= 10。
     */
    fun andWhereLessThanOrEqualTo(column: String, value: Number): WhereBuilder {
        return append(AND, column + LESS_THAN_OR_EQUAL_TO_HOLDER, arrayOf(value.toString()))
    }

    /**
     * 添加sql语句片段，如and a in(?,?,...)。
     */
    fun andWhereIn(column: String, values: Array<Any>): WhereBuilder {
        return appendWhereIn(AND, column, values)
    }

    /**
     * 添加sql语句片段，如or a = 0。
     */
    fun orWhereEqualTo(column: String, value: Any): WhereBuilder {
        return append(OR, column + EQUAL_HOLDER, arrayOf(value.toString()))
    }

    /**
     * 添加sql语句片段，如or a != 0。
     */
    fun orWhereNotEqualTo(column: String, value: Any): WhereBuilder {
        return append(OR, column + NOT_EQUAL_HOLDER, arrayOf(value.toString()))
    }

    /**
     * 添加sql语句片段，如or a > 0。
     */
    fun orWhereGreaterThan(column: String, value: Number): WhereBuilder {
        return append(OR, column + GREATER_THAN_HOLDER, arrayOf(value.toString()))
    }

    /**
     * 添加sql语句片段，如or a >= 0。
     */
    fun orWhereGreaterThanOrEqualTo(column: String, value: Number): WhereBuilder {
        return append(OR, column + GREATER_THAN_OR_EQUAL_TO_HOLDER, arrayOf(value.toString()))
    }

    /**
     * 添加sql语句片段，如or a < 10。
     */
    fun orWhereLessThan(column: String, value: Number): WhereBuilder {
        return append(OR, column + LESS_THAN_HOLDER, arrayOf(value.toString()))
    }

    /**
     * 添加sql语句片段，如or a <= 10。
     */
    fun orWhereLessThanOrEqualTo(column: String, value: Number): WhereBuilder {
        return append(OR, column + LESS_THAN_OR_EQUAL_TO_HOLDER, arrayOf(value.toString()))
    }

    /**
     * 添加sql语句片段，如or a in(?,?,...)。
     */
    fun orWhereIn(column: String, values: Array<Any>): WhereBuilder {
        return appendWhereIn(OR, column, values)
    }

    private fun append(connect: String?, whereClause: String, whereArgs: Array<Any>): WhereBuilder {
        if (connect != null) {
            selection += connect
        }
        selection += whereClause
        if (this.selectionArgs == null) {
            selectionArgs = arrayOfNulls(whereArgs.size)
            for (i in whereArgs.indices) {
                this.selectionArgs!![i] = whereArgs[i].toString()
            }
        } else {
            val tempArgs = arrayOfNulls<String>(selectionArgs!!.size + whereArgs.size)
            System.arraycopy(selectionArgs!!, 0, tempArgs, 0, selectionArgs!!.size)
            System.arraycopy(whereArgs, 0, tempArgs, selectionArgs!!.size, whereArgs.size)
            this.selectionArgs = tempArgs
        }
        return this
    }

    private fun appendWhereIn(connect: String?, column: String, values: Array<Any>): WhereBuilder {
        val whereIn = buildWhereIn(column, values.size)
        return append(connect, whereIn, values)
    }

    private fun buildWhereIn(column: String, num: Int): String {
        val sb = StringBuilder(column).append(SPACE).append(IN).append(PARENTHESES_LEFT)
                .append(HOLDER)
        for (i in 0 until num - 1) {
            sb.append(COMMA_HOLDER)
        }
        return sb.append(PARENTHESES_RIGHT).toString()
    }

    /**
     * 构建where的sql语句，自动添加语句首的where。
     */
    fun build(): String {
        return WHERE + selection
    }

    fun where(condition: Condition): WhereBuilder {
        selection = condition.selection
        selectionArgs = condition.selectionArgs
        return this
    }

    /**
     * 转化为Condition。
     */
    fun toCondition() : Condition {
        return Condition(selection, selectionArgs ?: arrayOf())
    }

    companion object {
        private const val WHERE = " WHERE "
        private const val EQUAL_HOLDER = "=?"
        private const val NOT_EQUAL_HOLDER = "!=?"
        private const val GREATER_THAN_HOLDER = ">?"
        private const val LESS_THAN_HOLDER = "<?"
        private const val GREATER_THAN_OR_EQUAL_TO_HOLDER = ">=?"
        private const val LESS_THAN_OR_EQUAL_TO_HOLDER = "<=?"
        private const val COMMA_HOLDER = ",?"
        private const val HOLDER = "?"
        private const val AND = " AND "
        private const val OR = " OR "
        private const val NOT = " NOT "
        private const val IN = " IN "
        private const val PARENTHESES_LEFT = "("
        private const val PARENTHESES_RIGHT = ")"
        private const val SPACE = " "

        fun create(): WhereBuilder {
            return WhereBuilder()
        }

        fun create(whereClause: String, whereArgs: Array<String?>?): WhereBuilder {
            return WhereBuilder(whereClause, whereArgs)
        }

        fun create(condition: Condition): WhereBuilder {
            return WhereBuilder(condition)
        }
    }
}