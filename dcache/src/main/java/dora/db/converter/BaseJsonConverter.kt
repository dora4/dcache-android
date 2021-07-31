package dora.db.converter

import com.google.gson.Gson
import java.lang.reflect.ParameterizedType

/**
 * 将对象映射成json进行保存，它的实现类同样需要实现PropertyConverter接口才能正常使用。
 */
abstract class BaseJsonConverter<T> : PropertyConverter<T, String> {

    override fun convertToEntityProperty(databaseValue: String?): T? {
        return Gson().fromJson(databaseValue, getGenericType(this)) as T
    }

    override fun convertToDatabaseValue(entityProperty: T?): String? {
        return Gson().toJson(entityProperty)
    }

    private fun getGenericType(obj: Any): Class<*>? {
        return if (obj.javaClass.genericSuperclass is ParameterizedType &&
                (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments.isNotEmpty()) {
            (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>
        } else (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>
    }
}