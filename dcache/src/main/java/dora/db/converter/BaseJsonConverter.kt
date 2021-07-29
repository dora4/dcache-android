package dora.db.converter

import com.google.gson.Gson
import dora.db.table.PropertyConverter

/**
 * 将对象映射成json进行保存。
 */
abstract class BaseJsonConverter<T> : PropertyConverter<T, String> {

    override fun convertToEntityProperty(databaseValue: String?): T? {
        return Gson().fromJson(databaseValue, javaClass.genericInterfaces[0])
    }

    override fun convertToDatabaseValue(entityProperty: T?): String? {
        return Gson().toJson(entityProperty)
    }
}