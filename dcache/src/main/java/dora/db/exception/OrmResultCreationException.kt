package dora.db.exception

/**
 * When querying data, this error is thrown because the implementation class of
 * [dora.db.table.OrmTable] cannot be created successfully. This is usually because the Kotlin data
 * class does not define a default value for the attribute instead of giving a parameter value when
 * passing the parameter.
 * 简体中文：查询数据时，因为[dora.db.table.OrmTable]的实现类无法成功创建而抛出。通常是由于kotlin的data class
 * 没有给属性定义默认值，而不是在传递参数时给定参数值。
 */
class OrmResultCreationException(message: String) : RuntimeException(message)