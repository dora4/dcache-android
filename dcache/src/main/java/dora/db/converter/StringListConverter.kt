package dora.db.converter

/**
 * Converter for mapping List<String> to a String for saving.
 * 简体中文：将List<String>映射成String进行保存的转换器。
 */
class StringListConverter : PropertyConverter<List<String>, String> {

    /**
     * Convert database values to an entity class. Decode the database values and assign them to
     * the entity class.
     * 简体中文：数据库的值转换为实体类。解析数据库的值解码后赋值给实体类。
     *
     * @param databaseValue Database values, such as "a,b,c". 简体中文：数据库的值，如"a,b,c"
     * @return A List containing three elements: a, b, and c. 简体中文：如存放了a,b,c三个元素的List
     */
    override fun convertToEntityProperty(databaseValue: String?): List<String>? {
        if (databaseValue == null) {
            return arrayListOf()
        }
        return databaseValue.split(",")
    }

    /**
     * Convert  an entity class to database values. Encode the entity class and save it to the
     * database.
     * 简体中文：实体类转换为数据库的值。将实体类编码后保存到数据库。
     *
     * @param entityProperty A List containing three elements: a, b, and c. 简体中文：如存放了a,b,c三个
     * 元素的List
     * @return Database values, such as "a,b,c". 简体中文：数据库的值，如"a,b,c"
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