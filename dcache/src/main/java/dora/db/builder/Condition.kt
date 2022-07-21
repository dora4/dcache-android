package dora.db.builder

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