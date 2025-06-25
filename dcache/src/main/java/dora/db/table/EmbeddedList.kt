package dora.db.table

/**
 * Marks a field as a list of embedded objects, indicating it is a one-to-many relationship to
 * another table.
 * During database operations, each item in this list will be inserted or updated individually.
 * This field does not require a foreign key annotation since the relation is represented by
 * multiple child entries.
 * 简体中文：标记一个字段为嵌套对象列表，表示该字段是一对多关联的子表集合。
 * 数据库操作时，会对列表中的每个对象逐条插入或更新。
 * 该字段不需要配置外键注解，因为关系由多条子表数据表示。
 *
 * @since 3.4.3
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class EmbeddedList

