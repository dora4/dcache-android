package dora.db.builder

import android.text.TextUtils

/**
 * 查询条件，它是一个和orm框架整合的协议。
 */
class Condition(
        // 条件语句，如 a > ? and a < ? , 或 a > 0
        val selection: String,
        // 条件参数，如 a > ? and a < ?中两个?的具体值0和100
                val selectionArgs: Array<String?>,
        // 同sql语句limit的用法
                val limit: String? = "",
        // 同sql语句order by的用法
                val orderBy: String? = "",
        // 同sql语句group by的用法
                val groupBy: String? = "",
        // 同sql语句having的用法
                val having: String? = "")


fun Condition.having(): String {
        return appendClause(" HAVING ", having)
}

fun Condition.orderBy(): String {
        return appendClause(" ORDER BY ", orderBy)
}

fun Condition.groupBy(): String {
        return appendClause(" GROUP BY ", groupBy)
}

fun Condition.limit(): String {
        return appendClause(" LIMIT ", limit)
}

private fun appendClause(name: String, clause: String?) : String {
        val s = StringBuilder()
        if (!TextUtils.isEmpty(clause)) {
                s.append(name)
                s.append(clause)
        }
        return s.toString()
}

/**
 * 将查询条件部分转化为sql。
 */
fun Condition.toSQL() : String {
        val sb = StringBuilder()
        if (selection != "") {
                if (!selection.contains("?")) {
                        sb.append("WHERE").append(" ").append(selection)
                } else {
                        val hasPrefixHolder = selection.startsWith("?")
                        if (hasPrefixHolder) {
                                throw IllegalArgumentException("selection can't start with ?")
                        }
                        val hasSuffixHolder = selection.endsWith("?")
                        val specs = selection.split("?")
                        specs.forEachIndexed {
                                        index, element ->
                                if (index <= specs.size - 2) {
                                        sb.append(element).append(selectionArgs[index])
                                }
                                if (hasSuffixHolder && index == specs.size - 1) {
                                        sb.append(element).append(selectionArgs[index])
                                }
                        }
                }
                sb.append(groupBy() + having() + orderBy() + limit())
        }
        return sb.toString()
}