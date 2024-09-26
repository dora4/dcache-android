package dora.db.exception

/**
 * Database state exception, typically thrown when the database has not been successfully created
 * or does not exist.
 * 简体中文：数据库状态异常，通常数据库还没有创建成功或不存在时才会抛此异常。
 */
class OrmStateException(message: String) : IllegalStateException(message)