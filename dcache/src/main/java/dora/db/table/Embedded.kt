package dora.db.table

/**
 * Marks a field as an embedded object, indicating it is a reference to another table (embedded relationship).
 * During database operations, this field will be automatically mapped to data from another table,
 * and associated through the specified `refId` field.
 * 简体中文：标记一个字段为嵌套对象，表示该字段是另一个表的引用（嵌入式关联）。
 * 数据库操作时，会自动将该字段映射为外部表的数据，并通过指定的 refId 字段建立关联。
 *
 * @param refId Specifies the field name in the current class that holds the foreign key value.
 *              简体中文：指定当前类中承接外键值的字段名。
 * @since 3.4.2
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Embedded(val refId: String)

