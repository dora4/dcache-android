package dora.db.exception

/**
 * Typically, it saves data types that are not supported by the ORM framework. Supported data types
 * include basic data types, CharSequence/String types, Class types, and complex data types
 * annotated with [dora.db.table.Convert].
 * 简体中文：通常是保存了Orm框架不支持的数据类型。支持的数据类型有基本数据类型、CharSequence/String类型、Class类型
 * 以及带[dora.db.table.Convert]注解的复杂数据类型。
 */
class UnsupportedDataTypeException(message: String) : UnsupportedOperationException(message)