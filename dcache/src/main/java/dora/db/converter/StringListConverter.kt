package dora.db.converter

import dora.db.table.PropertyConverter

class StringListConverter : PropertyConverter<List<String>, String> {

    /**
     * 解析数据库的值赋值给实体类。
     *
     * @param databaseValue 数据库的值，如"a,b,c"
     * @return 如存放了a,b,c三个元素的List
     */
    override fun convertToEntityProperty(databaseValue: String?): List<String>? {
        if (databaseValue == null) {
            return arrayListOf()
        }
        return databaseValue.split(",")
    }

    /**
     * 将复杂数据类型映射到数据库。
     *
     * @param entityProperty 如存放了a,b,c三个元素的List
     * @return 数据库的值，如"a,b,c"
     */
    override fun convertToDatabaseValue(entityProperty: List<String>?): String? {
        val sb = StringBuilder()
        entityProperty?.let {
            for (link in it) {
                sb.append(link)
                sb.append(",")
            }
        }
        return sb.toString()
    }
}