package dora.db.builder

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

    fun and(): WhereBuilder {
        if (selection != null) {
            selection += AND
        }
        return this
    }

    fun or(): WhereBuilder {
        if (selection != null) {
            selection += OR
        }
        return this
    }

    fun not(): WhereBuilder {
        if (selection != null) {
            selection += NOT
        } else {
            selection = NOT
        }
        return this
    }

    fun and(whereClause: String, vararg whereArgs: Any): WhereBuilder {
        return append(AND, whereClause, arrayOf(whereArgs))
    }

    fun or(whereClause: String, vararg whereArgs: Any): WhereBuilder {
        return append(OR, whereClause, arrayOf(whereArgs))
    }

    fun not(whereClause: String, vararg whereArgs: Any): WhereBuilder {
        return not().parenthesesLeft().append(null, whereClause, arrayOf(whereArgs)).parenthesesRight()
    }

    fun andNot(whereClause: String, whereArgs: Array<Any>): WhereBuilder {
        return and(not(whereClause, whereArgs))
    }

    fun orNot(whereClause: String, whereArgs: Array<Any>): WhereBuilder {
        return or(not(whereClause, whereArgs))
    }

    fun and(builder: WhereBuilder): WhereBuilder {
        val selection = builder.selection
        val selectionArgs = builder.selectionArgs
        return and(selection, selectionArgs!!)
    }

    fun or(builder: WhereBuilder): WhereBuilder {
        val selection = builder.selection
        val selectionArgs = builder.selectionArgs
        return or(selection, selectionArgs!!)
    }

    fun not(builder: WhereBuilder): WhereBuilder {
        val selection = builder.selection
        val selectionArgs = builder.selectionArgs
        return not(selection, selectionArgs!!)
    }

    fun andNot(builder: WhereBuilder): WhereBuilder {
        return and(not(builder))
    }

    fun orNot(builder: WhereBuilder): WhereBuilder {
        return or(not(builder))
    }

    fun parenthesesLeft(): WhereBuilder {
        if (selection != null) {
            selection += PARENTHESES_LEFT
        } else {
            selection = PARENTHESES_LEFT
        }
        return this
    }

    fun parenthesesRight(): WhereBuilder {
        if (selection != null) {
            selection += PARENTHESES_RIGHT
        }
        return this
    }

    fun addWhereEqualTo(column: String, value: Any): WhereBuilder {
        return append(null, column + EQUAL_HOLDER, arrayOf(value))
    }

    fun addWhereNotEqualTo(column: String, value: Any): WhereBuilder {
        return append(null, column + NOT_EQUAL_HOLDER, arrayOf(value))
    }

    fun addWhereGreaterThan(column: String, value: Number): WhereBuilder {
        return append(null, column + GREATER_THAN_HOLDER, arrayOf(value))
    }

    fun addWhereGreaterThanOrEqualTo(column: String, value: Number): WhereBuilder {
        return append(null, column + GREATER_THAN_OR_EQUAL_TO_HOLDER, arrayOf(value))
    }

    fun addWhereLessThan(column: String, value: Number): WhereBuilder {
        return append(null, column + LESS_THAN_HOLDER, arrayOf(value))
    }

    fun addWhereLessThanOrEqualTo(column: String, value: Number): WhereBuilder {
        return append(null, column + LESS_THAN_OR_EQUAL_TO_HOLDER, arrayOf(value))
    }

    fun addWhereIn(column: String, values: Array<Any>): WhereBuilder {
        return appendWhereIn(null, column, values)
    }

    fun andWhereEqualTo(column: String, value: Any): WhereBuilder {
        return append(AND, column + EQUAL_HOLDER, arrayOf(value))
    }

    fun andWhereNotEqualTo(column: String, value: Any): WhereBuilder {
        return append(AND, column + NOT_EQUAL_HOLDER, arrayOf(value))
    }

    fun andWhereGreatorThan(column: String, value: Number): WhereBuilder {
        return append(AND, column + GREATER_THAN_HOLDER, arrayOf(value))
    }

    fun andWhereGreatorThanOrEqualTo(column: String, value: Number): WhereBuilder {
        return append(AND, column + GREATER_THAN_OR_EQUAL_TO_HOLDER, arrayOf(value))
    }

    fun andWhereLessThan(column: String, value: Number): WhereBuilder {
        return append(AND, column + LESS_THAN_HOLDER, arrayOf(value))
    }

    fun andWhereLessThanOrEqualTo(column: String, value: Number): WhereBuilder {
        return append(AND, column + LESS_THAN_OR_EQUAL_TO_HOLDER, arrayOf(value))
    }

    fun andWhereIn(column: String, values: Array<Any>): WhereBuilder {
        return appendWhereIn(AND, column, values)
    }

    fun orWhereEqualTo(column: String, value: Any): WhereBuilder {
        return append(OR, column + EQUAL_HOLDER, arrayOf(value))
    }

    fun orWhereNotEqualTo(column: String, value: Any): WhereBuilder {
        return append(OR, column + NOT_EQUAL_HOLDER, arrayOf(value))
    }

    fun orWhereGreatorThan(column: String, value: Number): WhereBuilder {
        return append(OR, column + GREATER_THAN_HOLDER, arrayOf(value))
    }

    fun orWhereGreatorThanOrEqualTo(column: String, value: Number): WhereBuilder {
        return append(OR, column + GREATER_THAN_OR_EQUAL_TO_HOLDER, arrayOf(value))
    }

    fun orWhereLessThan(column: String, value: Number): WhereBuilder {
        return append(OR, column + LESS_THAN_HOLDER, arrayOf(value))
    }

    fun orWhereLessThanOrEqualTo(column: String, value: Number): WhereBuilder {
        return append(OR, column + LESS_THAN_OR_EQUAL_TO_HOLDER, arrayOf(value))
    }

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
            val tempArgs = arrayOfNulls<String>(this.selectionArgs!!.size + whereArgs.size)
            System.arraycopy(this.selectionArgs, 0, tempArgs, 0, this.selectionArgs!!.size)
            System.arraycopy(whereArgs, 0, tempArgs, this.selectionArgs!!.size, whereArgs.size)
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

    fun build(): String {
        return WHERE + selection
    }

    fun where(condition: Condition): WhereBuilder {
        selection = condition.selection
        selectionArgs = condition.selectionArgs
        return this
    }

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