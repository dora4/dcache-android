package dora.db.table

/**
 * Use it to specify member properties in the implementation class of [dora.db.table.OrmTable] that
 * do not need to be created as columns when creating the table; it has a higher priority than
 * [dora.db.table.Column].
 * 简体中文：用它指定[dora.db.table.OrmTable]的实现类中不需要在创建表时创建列的成员属性，优先级比
 * [dora.db.table.Column]要高。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Ignore 