package dora.db.builder

class Condition(val selection: String, val selectionArgs: Array<String?>,
                val limit: String? = "",
                val orderBy: String? = "",
                val groupBy: String? = "",
                val having: String? = "")