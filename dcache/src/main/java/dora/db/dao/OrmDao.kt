package dora.db.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import dora.db.Orm
import dora.db.OrmLog
import dora.db.async.OrmExecutor
import dora.db.async.OrmTask
import dora.db.async.OrmTaskListener
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.db.constraint.Id
import dora.db.constraint.PrimaryKey
import dora.db.constraint.AssignType
import dora.db.constraint.ForeignKey
import dora.db.converter.PropertyConverter
import dora.db.exception.OrmResultCreationException
import dora.db.exception.UnsupportedDataTypeException
import dora.db.table.*
import java.lang.reflect.*
import java.util.*
import java.util.concurrent.Callable

/**
 * An object-oriented database operation encapsulation class, based on an OrmTable implementation
 * class, used to operate a table.
 * 简体中文：一个面向对象的数据库操作的封装类，基于一个OrmTable的实现类，用来操作一张表。
 */
class OrmDao<T : OrmTable> internal @JvmOverloads constructor(
    private val beanClass: Class<T>,
    db: SQLiteDatabase? = null
) : Dao<T> {

    /**
     * This object can be obtained only after the database is prepared.
     * 简体中文：数据库准备完成后才能获取到这个对象。
     */
    private val database: SQLiteDatabase = db ?: Orm.getDB()

    private val executor: OrmExecutor<T> by lazy { OrmExecutor<T>() }

    /**
     * It is used to retain the operation mode of sql statements.
     * 简体中文：它用来保留sql语句的操作方式。
     */
    fun getDB() : SQLiteDatabase {
        return database
    }

    /**
     * Checks whether the attribute is of type boolean.
     * 简体中文：检测属性是否是boolean类型。
     */
    private fun isAssignableFromBoolean(fieldType: Class<*>): Boolean {
        return Boolean::class.javaPrimitiveType!!.isAssignableFrom(fieldType) ||
                Boolean::class.java.isAssignableFrom(fieldType)
    }

    /**
     * Checks whether the attribute is of type byte.
     * 简体中文：检测属性是否是byte类型。
     */
    private fun isAssignableFromByte(fieldType: Class<*>): Boolean {
        return Byte::class.javaPrimitiveType!!.isAssignableFrom(fieldType) ||
                Byte::class.java.isAssignableFrom(fieldType)
    }

    /**
     * Checks whether the attribute is of type short.
     * 简体中文：检测属性是否是short类型。
     */
    private fun isAssignableFromShort(fieldType: Class<*>): Boolean {
        return Short::class.javaPrimitiveType!!.isAssignableFrom(fieldType) ||
                Short::class.java.isAssignableFrom(fieldType)
    }

    /**
     * Checks whether the attribute is of type int.
     * 简体中文：检测属性是否是int类型。
     */
    private fun isAssignableFromInteger(fieldType: Class<*>): Boolean {
        return Int::class.javaPrimitiveType!!.isAssignableFrom(fieldType) ||
                Int::class.java.isAssignableFrom(fieldType)
    }

    /**
     * Checks whether the attribute is of type long.
     * 简体中文：检测属性是否是long类型。
     */
    private fun isAssignableFromLong(fieldType: Class<*>): Boolean {
        return Long::class.javaPrimitiveType!!.isAssignableFrom(fieldType) ||
                Long::class.java.isAssignableFrom(fieldType)
    }

    /**
     * Checks whether the attribute is of type float.
     * 简体中文：检测属性是否是float类型。
     */
    private fun isAssignableFromFloat(fieldType: Class<*>): Boolean {
        return Float::class.javaPrimitiveType!!.isAssignableFrom(fieldType) ||
                Float::class.java.isAssignableFrom(fieldType)
    }

    /**
     * Checks whether the attribute is of type double.
     * 简体中文：检测属性是否是double类型。
     */
    private fun isAssignableFromDouble(fieldType: Class<*>): Boolean {
        return Double::class.javaPrimitiveType!!.isAssignableFrom(fieldType) ||
                Double::class.java.isAssignableFrom(fieldType)
    }

    /**
     * Checks whether the attribute is of type char.
     * 简体中文：检测属性是否是char类型。
     */
    private fun isAssignableFromCharacter(fieldType: Class<*>): Boolean {
        return Char::class.javaPrimitiveType!!.isAssignableFrom(fieldType) ||
                Char::class.java.isAssignableFrom(fieldType)
    }

    /**
     * Checks whether the attribute is of type string.
     * 简体中文：检测属性是否是字符串类型。
     */
    private fun isAssignableFromCharSequence(fieldType: Class<*>): Boolean {
        return CharSequence::class.java.isAssignableFrom(fieldType) ||
                String::class.java.isAssignableFrom(fieldType)
    }

    /**
     * Checks whether the attribute is of type class.
     * 简体中文：检测属性是否是Class类型。
     */
    private fun isAssignableFromClass(fieldType: Class<*>): Boolean {
        return Class::class.java.isAssignableFrom(fieldType)
    }

    /**
     * Check if this is a reserved attribute of OrmTable.
     * 简体中文：检测是否是OrmTable的保留属性。
     */
    private fun isOrmTableField(field: Field) : Boolean {
        return field.name.equals("isUpgradeRecreated") || field.name.equals("migrations")
    }

    /**
     * Used to convert boolean type into int type and save it.
     * 简体中文：用来将boolean类型转换成int类型保存起来。
     */
    private fun convertBooleanToInt(boolean: Boolean) : Int {
        return if (boolean) 1 else 0
    }

    /**
     * Convert entity classes into ContentValues object。
     * 简体中文：将实体类转换成ContentValues对象。
     */
    private fun getContentValues(bean: T): ContentValues {
        val values = ContentValues()
        val fields = beanClass.declaredFields
        for (embeddedField in fields) {
            val embedded = embeddedField.getAnnotation(Embedded::class.java) ?: continue
            val refIdName = embedded.refId
            val foreignKeyField = fields.firstOrNull {
                it.getAnnotation(ForeignKey::class.java)?.tableClass == embeddedField.type &&
                        it.name == refIdName
            } ?: throw IllegalArgumentException("No @ForeignKey field found matching Embedded refId $refIdName")
            embeddedField.isAccessible = true
            foreignKeyField.isAccessible = true
        }
        for (field in fields) {
            field.isAccessible = true
            val embedded: Embedded? = field.getAnnotation(Embedded::class.java)
            val foreignKey: ForeignKey? = field.getAnnotation(ForeignKey::class.java)
            val ignore: Ignore? = field.getAnnotation(Ignore::class.java)
            val id: Id? = field.getAnnotation(Id::class.java)
            val column: Column? = field.getAnnotation(Column::class.java)
            val primaryKey: PrimaryKey? = field.getAnnotation(PrimaryKey::class.java)
            val convert: Convert? = field.getAnnotation(Convert::class.java)
            if (embedded != null) continue
            if (foreignKey != null) continue
            if (ignore != null || field.modifiers and Modifier.STATIC != 0) {
                continue
            }
            if (id != null) continue
            if (primaryKey != null && primaryKey.value === AssignType.AUTO_INCREMENT) {
                continue
            }
            if (isOrmTableField(field)) {
                continue
            }
            val columnName: String = column?.value ?: TableManager.generateColumnName(field.name)
            val fieldType: Class<*> = convert?.columnType?.java ?: field.type
            if (isAssignableFromCharSequence(fieldType)) {
                if (convert != null) {
                    val value = field[bean]
                    val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                    val propertyConverter: PropertyConverter<Any, String> = Proxy
                        .newProxyInstance(converter.classLoader,
                            converter.interfaces, PropertyHandler(converter))
                            as PropertyConverter<Any, String>
                    value?.let {
                        values.put(columnName, propertyConverter.convertToDatabaseValue(it))
                    }
                } else {
                    values.put(columnName, field[bean]?.toString().orEmpty())
                }
            } else if (isAssignableFromBoolean(fieldType)) {
                if (convert != null) {
                    val value = field[bean]
                    val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                    val propertyConverter: PropertyConverter<Any, Boolean> = Proxy
                        .newProxyInstance(converter.classLoader,
                            converter.interfaces, PropertyHandler(converter))
                            as PropertyConverter<Any, Boolean>
                    value?.let {
                        values.put(columnName, convertBooleanToInt(
                            propertyConverter.convertToDatabaseValue(it) as Boolean))
                    }
                } else {
                    values.put(columnName, convertBooleanToInt(field.getBoolean(bean)))
                }
            } else if (isAssignableFromByte(fieldType)) {
                if (convert != null) {
                    val value = field[bean]
                    val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                    val propertyConverter: PropertyConverter<Any, Byte> = Proxy
                        .newProxyInstance(converter.classLoader,
                            converter.interfaces, PropertyHandler(converter))
                            as PropertyConverter<Any, Byte>
                    value?.let {
                        values.put(columnName, propertyConverter.convertToDatabaseValue(it))
                    }
                } else {
                    values.put(columnName, field.getByte(bean))
                }
            } else if (isAssignableFromShort(fieldType)) {
                if (convert != null) {
                    val value = field[bean]
                    val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                    val propertyConverter: PropertyConverter<Any, Short> = Proxy
                        .newProxyInstance(converter.classLoader,
                            converter.interfaces, PropertyHandler(converter))
                            as PropertyConverter<Any, Short>
                    value?.let {
                        values.put(columnName, propertyConverter.convertToDatabaseValue(it))
                    }
                } else {
                    values.put(columnName, field.getShort(bean))
                }
            } else if (isAssignableFromInteger(fieldType)) {
                if (convert != null) {
                    val value = field[bean]
                    val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                    val propertyConverter: PropertyConverter<Any, Int> = Proxy
                        .newProxyInstance(converter.classLoader,
                            converter.interfaces, PropertyHandler(converter))
                            as PropertyConverter<Any, Int>
                    value?.let {
                        values.put(columnName, propertyConverter.convertToDatabaseValue(it))
                    }
                } else {
                    values.put(columnName, field.getInt(bean))
                }
            } else if (isAssignableFromLong(fieldType)) {
                if (convert != null) {
                    val value = field[bean]
                    val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                    val propertyConverter: PropertyConverter<Any, Long> = Proxy
                        .newProxyInstance(converter.classLoader,
                            converter.interfaces, PropertyHandler(converter))
                            as PropertyConverter<Any, Long>
                    value?.let {
                        values.put(columnName, propertyConverter.convertToDatabaseValue(it))
                    }
                } else {
                    values.put(columnName, field.getLong(bean))
                }
            } else if (isAssignableFromFloat(fieldType)) {
                if (convert != null) {
                    val value = field[bean]
                    val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                    val propertyConverter: PropertyConverter<Any, Float> = Proxy
                        .newProxyInstance(converter.classLoader,
                            converter.interfaces, PropertyHandler(converter))
                            as PropertyConverter<Any, Float>
                    value?.let {
                        values.put(columnName, propertyConverter.convertToDatabaseValue(it))
                    }
                } else {
                    values.put(columnName, field.getFloat(bean))
                }
            } else if (isAssignableFromDouble(fieldType)) {
                if (convert != null) {
                    val value = field[bean]
                    val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                    val propertyConverter: PropertyConverter<Any, Double> = Proxy
                        .newProxyInstance(converter.classLoader,
                            converter.interfaces, PropertyHandler(converter))
                            as PropertyConverter<Any, Double>
                    value?.let {
                        values.put(columnName, propertyConverter.convertToDatabaseValue(it))
                    }
                } else {
                    values.put(columnName, field.getDouble(bean))
                }
            } else if (isAssignableFromCharacter(fieldType)) {
                if (convert != null) {
                    val value = field[bean]
                    val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                    val propertyConverter: PropertyConverter<Any, Char> = Proxy
                        .newProxyInstance(converter.classLoader,
                        converter.interfaces, PropertyHandler(converter))
                            as PropertyConverter<Any, Char>
                    value?.let {
                        values.put(columnName, propertyConverter.convertToDatabaseValue(it).toString())
                    }
                } else {
                    values.put(columnName, field.getChar(bean).toString())
                }
            } else if (isAssignableFromClass(fieldType)) {
                if (convert != null) {
                    val value = field[bean]
                    val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                    val propertyConverter: PropertyConverter<Any, Class<*>> = Proxy
                        .newProxyInstance(converter.classLoader,
                        converter.interfaces, PropertyHandler(converter))
                            as PropertyConverter<Any, Class<*>>
                    value?.let {
                        values.put(columnName, propertyConverter.convertToDatabaseValue(it)?.name.orEmpty())
                    }
                } else {
                    values.put(columnName, (field.get(bean) as Class<*>).name)
                }
            } else {
                throw UnsupportedDataTypeException("$columnName is using an unsupported type:" +
                        " ${fieldType.name} . Please use the Convert annotation or specify a" +
                        " supported type for columnType in the Convert annotation.")
            }
        }
        return values
    }

    private val columnHack: String
        get() {
            val sb = StringBuilder()
            val fields = beanClass.declaredFields
            for (field in fields) {
                field.isAccessible = true
                val ignore: Ignore? = field.getAnnotation(Ignore::class.java)
                val primaryKey: PrimaryKey? = field.getAnnotation(PrimaryKey::class.java)
                val id: Id? = field.getAnnotation(Id::class.java)
                if (ignore == null && (primaryKey == null && id == null ||
                                primaryKey != null && primaryKey.value === AssignType.BY_MYSELF)) {
                    val name = field.name
                    sb.append(name).append(",")
                }
            }
            return sb.substring(0, sb.length - 2)
        }

    /**
     * Insert a record.
     * 简体中文：插入一条数据。
     */
    override fun insert(bean: T): Boolean {
        return insertInternal(bean, database)
    }

    private fun insertOrUpdateUncheckedCast(bean: Any): Long {
        if (!beanClass.isInstance(bean)) {
            throw IllegalArgumentException("bean must be instance of ${beanClass.name}")
        }
        @Suppress("UNCHECKED_CAST")
        return insertOrUpdateReturnId(bean as T)
    }

    private fun insertReturnId(bean: T): Long {
        val contentValues = getContentValues(bean)
        val tableName = TableManager.getTableName(beanClass)
        val rowId = database.insert(tableName, null, contentValues)
        if (rowId > 0) {
            val idField = beanClass.declaredFields.firstOrNull { it.getAnnotation(Id::class.java) != null }
            idField?.let {
                it.isAccessible = true
                it.set(bean, rowId)
            }
        }
        return rowId
    }

    private fun insertOrUpdateReturnId(bean: T): Long {
        val idField = beanClass.declaredFields.firstOrNull { it.getAnnotation(Id::class.java) != null }
            ?: throw IllegalArgumentException("Bean class must have a field annotated with @Id")

        idField.isAccessible = true
        val currentId = idField.get(bean) as? Long ?: 0L

        return if (currentId <= 0L) {
            insertReturnId(bean)
        } else {
            val updated = update(bean)
            if (updated) currentId else -1L
        }
    }

    private fun processRelationsInsertOrUpdate(bean: T) {
        val fields = beanClass.declaredFields
        for (field in fields) {
            field.isAccessible = true

            val embedded = field.getAnnotation(Embedded::class.java)
            if (embedded != null) {
                if (!OrmTable::class.java.isAssignableFrom(field.type)) {
                    throw IllegalArgumentException("Field '${field.name}' annotated with @Embedded must be a subtype of OrmTable")
                }
                val subTableBean = field.get(bean) ?: continue
                val refIdName = embedded.refId
                val refIdField = fields.firstOrNull { it.name == refIdName }
                    ?: throw IllegalArgumentException("Missing foreign key field '$refIdName' for @Embedded on '${field.name}'")

                refIdField.isAccessible = true
                val foreignKey = refIdField.getAnnotation(ForeignKey::class.java)
                    ?: throw IllegalArgumentException("Foreign key field '$refIdName' must be annotated with @ForeignKey")
                if (foreignKey.tableClass != field.type) {
                    throw IllegalArgumentException("Foreign key field @ForeignKey.tableClass does not " +
                            "match the type of the property annotated with @Embedded, field '${field.name}'.")
                }
                if (!isAssignableFromLong(refIdField.type)) {
                    throw IllegalArgumentException("@ForeignKey field '$refIdName' must be of type Long")
                }

                val subTableClass: Class<out OrmTable> = field.type as Class<out OrmTable>
                val dao = DaoFactory.getDao(subTableClass, database)

                val subIdField = subTableClass.declaredFields.firstOrNull {
                    it.getAnnotation(Id::class.java) != null
                } ?: throw IllegalArgumentException("Sub-table class ${subTableClass.simpleName}" +
                        " must have a field annotated with @Id")
                subIdField.isAccessible = true

                val generatedId = dao.insertOrUpdateUncheckedCast(subTableBean)
                if (generatedId <= 0) {
                    throw IllegalArgumentException("Insert or update sub table failed for field '${field.name}'")
                }
                refIdField.set(bean, generatedId)
                continue
            }

            val embeddedList = field.getAnnotation(EmbeddedList::class.java)
            if (embeddedList != null) {
                if (!List::class.java.isAssignableFrom(field.type) && !MutableList::class.java.isAssignableFrom(field.type)) {
                    throw IllegalArgumentException("Field '${field.name}' annotated with @EmbeddedList must be of type List<*> or MutableList<*>")
                }
                val genericType = (field.genericType as? ParameterizedType)
                    ?.actualTypeArguments?.firstOrNull() as? Class<*>
                    ?: throw IllegalArgumentException("Field '${field.name}' must specify a generic type for List<*>")
                if (!OrmTable::class.java.isAssignableFrom(genericType)) {
                    throw IllegalArgumentException("Type parameter of List in '${field.name}' must be subtype of OrmTable")
                }

                val subTableClass = genericType as Class<out OrmTable>
                val dao = DaoFactory.getDao(subTableClass, database)

                val list = field.get(bean) as? List<*> ?: continue
                for (item in list) {
                    if (item == null) continue
                    val itemClass = item.javaClass
                    val idField = itemClass.declaredFields.firstOrNull {
                        it.getAnnotation(Id::class.java) != null
                    } ?: throw IllegalArgumentException("Sub-table class ${itemClass.simpleName}" +
                            " must have a field annotated with @Id")
                    idField.isAccessible = true
                    dao.insertOrUpdateUncheckedCast(item)
                }
            }
        }
    }

    /**
     * Insert a record.
     * 简体中文：插入一条数据。
     */
    private fun insertInternal(bean: T, db: SQLiteDatabase): Boolean {
        processRelationsInsertOrUpdate(bean)
        val tableName: String = TableManager.getTableName(beanClass)
        val contentValues = getContentValues(bean)
        return db.insert(tableName, columnHack, contentValues) > 0
    }

    /**
     * Insert multiple records.
     * 简体中文：插入多条数据。
     */
    override fun insert(beans: List<T>): Boolean {
        return insertInternal(beans, database)
    }

    /**
     * Insert multiple records.
     * 简体中文：插入多条数据。
     */
    private fun insertInternal(beans: List<T>, db: SQLiteDatabase): Boolean {
        var count = 0
        for (bean in beans) {
            val isOk = insertInternal(bean, db)
            if (isOk) {
                count++
            }
        }
        return count == beans.size
    }

    /**
     * Insert a record.
     * 简体中文：插入一条数据。
     */
    override fun insertAsync(bean: T, listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.Insert, this, bean,
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Insert multiple records.
     * 简体中文：插入多条数据。
     */
    override fun insertAsync(beans: List<T>, listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.InsertList, this, beans,
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Delete data based on conditions.
     * 简体中文：按条件删除数据。
     */
    override fun delete(builder: WhereBuilder): Boolean {
        return deleteInternal(builder, database)
    }

    /**
     * Delete data based on conditions.
     * 简体中文：按条件删除数据。
     */
    override fun deleteAsync(builder: WhereBuilder, listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.Delete, this, builder,
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Delete a record.
     * 简体中文：删除一条数据。
     */
    override fun deleteAsync(bean: T, listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.DeleteByKey, this, bean,
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Delete a record.
     * 简体中文：删除一条数据。
     */
    override fun delete(bean: T): Boolean {
        val field = bean.javaClass.getDeclaredField(getPrimaryKeyFieldName(bean))
        field.isAccessible = true
        val name = TableManager.getColumnName(field)
        val value = field.get(bean)?.toString()
        return deleteInternal(WhereBuilder.create(Condition("$name=?",
            arrayOf(value))), database)
    }

    /**
     * Delete all records.
     * 简体中文：删除所有数据。
     */
    override fun deleteAll(): Boolean {
        return deleteAllInternal(database)
    }

    /**
     * Delete all records.
     * 简体中文：删除所有数据。
     */
    override fun deleteAllAsync(listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.DeleteAll, this, null,
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Update a record.
     * 简体中文：更新一条数据。
     */
    override fun updateAsync(bean: T, listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.UpdateByKey, this, bean,
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Update all data that meets the conditions to [newBean].
     * 简体中文：更新所有满足条件的数据为[newBean]。
     */
    override fun updateAsync(builder: WhereBuilder, newBean: T, listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.WhereInsertOrReplace, this,
            Callable() {
                update(builder, newBean)
            },
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Insert or update data. If it exists, update it; if not, insert it.
     * 简体中文：插入或更新数据。如果有，则更新，没有，则插入。
     */
    override fun insertOrUpdateAsync(bean: T, listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.InsertOrReplace, this, bean,
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Query all data that meets the conditions; if any are found, update them to [newBean]; if
     * none are found, insert a new [newBean].
     * 简体中文：查询所有满足条件的数据，如果有，则全部更新为[newBean]，没有，则插入一个[newBean]。
     */
    override fun insertOrUpdateAsync(builder: WhereBuilder, newBean: T, listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.WhereInsertOrReplace, this,
            Callable() {
                insertOrUpdate(builder, newBean)
                       },
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Delete all records.
     * 简体中文：删除所有数据。
     */
    private fun deleteAllInternal(db: SQLiteDatabase): Boolean {
        val tableName: String = TableManager.getTableName(beanClass)
        return db.delete(tableName, null, null) > 0
    }

    /**
     * Delete data based on conditions.
     * 简体中文：按条件删除数据。
     */
    private fun deleteInternal(builder: WhereBuilder, db: SQLiteDatabase): Boolean {
        val tableName: String = TableManager.getTableName(beanClass)
        return db.delete(tableName, builder.selection, builder.selectionArgs) > 0
    }

    /**
     * Query all data that meets the conditions; if any are found, update them to [newBean]; if
     * none are found, insert a new [newBean].
     * 简体中文：查询所有满足条件的数据，如果有，则全部更新为[newBean]，没有，则插入一个[newBean]。
     */
    private fun insertOrUpdateInternal(builder: WhereBuilder, newBean: T): Boolean {
        val result = select(builder);
        if (result.isEmpty()) {
            return insert(newBean)
        } else {
            val size = result.size
            var count = 0
            result.forEach {
                val clazz: Class<T> = it.javaClass
                for (field in clazz.declaredFields) {
                    field.isAccessible = true
                    val value = field[it]
                    field[newBean] = value
                }
                val ok = update(newBean)
                if (ok) {
                    count++
                }
            }
            return count == size
        }
    }

    /**
     * Query all data that meets the conditions; if any are found, update them to [newBean]; if
     * none are found, insert a new [newBean].
     * 简体中文：查询所有满足条件的数据，如果有，则全部更新为[newBean]，没有，则插入一个[newBean]。
     */
    override fun insertOrUpdate(builder: WhereBuilder, newBean: T): Boolean {
        return insertOrUpdateInternal(builder, newBean)
    }

    /**
     * Insert or update data. If it exists, update it; if not, insert it.
     * 简体中文：插入或更新数据。如果有，则更新，没有，则插入。
     */
    private fun insertOrUpdateInternal(bean: T): Boolean {
        processRelationsInsertOrUpdate(bean)
        val field = bean.javaClass.getDeclaredField(getPrimaryKeyFieldName(bean))
        field.isAccessible = true
        val name = TableManager.getColumnName(field)
        val value = field.get(bean)
        val result = selectOne(WhereBuilder.create().addWhereEqualTo(name, value))
        return if (result != null) {
            update(bean)
        } else {
            insert(bean)
        }
    }

    /**
     * Insert or update data. If it exists, update it; if not, insert it.
     * 简体中文：插入或更新数据。如果有，则更新，没有，则插入。
     */
    override fun insertOrUpdate(bean: T): Boolean {
        return insertOrUpdateInternal(bean)
    }

    /**
     * Update all data that meets the conditions to [newBean].
     * 简体中文：更新所有满足条件的数据为[newBean]。
     */
    override fun update(builder: WhereBuilder, newBean: T): Boolean {
        return updateInternal(builder, newBean, database)
    }

    /**
     * Update a record.
     * 简体中文：更新一条数据。
     */
    override fun update(bean: T): Boolean {
        val field = bean.javaClass.getDeclaredField(getPrimaryKeyFieldName(bean))
        field.isAccessible = true
        val name = TableManager.getColumnName(field)
        val value = field.get(bean)?.toString()
        return updateInternal(WhereBuilder.create(Condition("$name=?", arrayOf(value))),
                bean, database)
    }

    /**
     * Query all data in the table.
     * 简体中文：查询该表中的所有数据。
     */
    private fun updateAllInternal(newBean: T, db: SQLiteDatabase): Boolean {
        processRelationsInsertOrUpdate(newBean)
        val tableName: String = TableManager.getTableName(beanClass)
        val contentValues = getContentValues(newBean)
        return db.update(tableName, contentValues, null, null) > 0
    }

    /**
     * Update a record.
     * 简体中文：更新一条数据。
     */
    private fun updateInternal(builder: WhereBuilder, newBean: T, db: SQLiteDatabase): Boolean {
        processRelationsInsertOrUpdate(newBean)
        val tableName: String = TableManager.getTableName(beanClass)
        val contentValues = getContentValues(newBean)
        return db.update(tableName, contentValues, builder.selection, builder.selectionArgs) > 0
    }

    /**
     * Query all data in the table.
     * 简体中文：查询该表中的所有数据。
     */
    override fun selectAll(): List<T> {
        return selectAllInternal()
    }

    /**
     * Query all data in the table.
     * 简体中文：查询该表中的所有数据。
     */
    override fun selectAllAsync(listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.QueryAll, this, null,
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Query all data in the table.
     * 简体中文：查询该表中的所有数据。
     */
    private fun selectAllInternal(): List<T> {
        val tableName: String = TableManager.getTableName(beanClass)
        val cursor = database.query(tableName, null, null,
            null, null, null, null)
        return getResult(cursor)
    }

    /**
     * Query data based on conditions.
     * 简体中文：按条件查询数据。
     */
    override fun select(builder: WhereBuilder): List<T> {
        return select(QueryBuilder.create().where(builder))
    }

    /**
     * Query data based on conditions.
     * 简体中文：按条件查询数据。
     */
    override fun select(builder: QueryBuilder): List<T> {
        return selectInternal(builder)
    }

    /**
     * Query data based on conditions.
     * 简体中文：按条件查询数据。
     */
    override fun selectAsync(builder: QueryBuilder, listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.QueryList, this, builder,
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Query data based on conditions.
     * 简体中文：按条件查询数据。
     */
    override fun selectAsync(builder: WhereBuilder, listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.WhereList, this, builder,
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Query data based on conditions.
     * 简体中文：按条件查询数据。
     */
    private fun selectInternal(builder: QueryBuilder): List<T> {
        val tableName: String = TableManager.getTableName(beanClass)
        val columns: Array<String>? = builder.getColumns()
        val group: String = builder.getGroup()
        val having: String = builder.getHaving()
        val order: String = builder.getOrder()
        val limit: String = builder.getLimit()
        val where: WhereBuilder = builder.getWhereBuilder()
        val selection: String = where.selection
        val selectionArgs: Array<String?>? = where.selectionArgs
        val cursor = database.query(tableName, columns, selection, selectionArgs, group, having, order, limit)
        return getResult(cursor)
    }

    /**
     * Query a specific record.
     * 简体中文：查询特定的一条数据。
     */
    override fun selectOne(): T? {
        return selectOneInternal()
    }

    /**
     * Query a specific record.
     * 简体中文：查询特定的一条数据。
     */
    private fun selectOneInternal(): T? {
        val tableName: String = TableManager.getTableName(beanClass)
        val cursor = database.query(tableName, null, null, null,
            null, null, null)
        if (cursor.moveToFirst()) {
            try {
                return createResult(cursor)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
        }
        return null
    }

    /**
     * Query a specific record.
     * 简体中文：查询特定的一条数据。
     */
    override fun selectOne(builder: WhereBuilder): T? {
        return selectOne(QueryBuilder.create().where(builder))
    }

    /**
     * Query a specific record.
     * 简体中文：查询特定的一条数据。
     */
    private fun selectOneInternal(builder: QueryBuilder): T? {
        val beans: List<T> = select(builder)
        if (beans.isNotEmpty()) {
            return beans[0]
        }
        val tableName: String = TableManager.getTableName(beanClass)
        val columns: Array<String>? = builder.getColumns()
        val group: String = builder.getGroup()
        val having: String = builder.getHaving()
        val order: String = builder.getOrder()
        val limit: String = builder.getLimit()
        val where: WhereBuilder = builder.getWhereBuilder()
        val selection: String = where.selection
        val selectionArgs: Array<String?>? = where.selectionArgs
        val cursor = database.query(tableName, columns, selection, selectionArgs, group, having,
            order, limit)
        if (cursor.moveToFirst()) {
            try {
                return createResult(cursor)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
        }
        return null
    }

    /**
     * Query a specific record.
     * 简体中文：查询特定的一条数据。
     */
    override fun selectOne(builder: QueryBuilder): T? {
        return selectOneInternal(builder)
    }

    /**
     * Query a specific record.
     * 简体中文：查询特定的一条数据。
     */
    override fun selectOneAsync(listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.IndexUnique, this, null,
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Query the record that matches the conditions.
     * 简体中文：查询符合条件的一条数据。
     */
    override fun selectOneAsync(builder: WhereBuilder, listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.WhereUnique, this, builder,
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Query the record that matches the conditions.
     * 简体中文：查询符合条件的一条数据。
     */
    override fun selectOneAsync(builder: QueryBuilder, listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.QueryUnique, this, builder,
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Count the number of query results.
     * 简体中文：统计查询的结果数量。
     */
    @Deprecated("Please use count() instead.", level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("count()")
    )
    override fun selectCount(): Long {
        return count()
    }

    /**
     * Count the number of query results.
     * 简体中文：统计查询的结果数量。
     */
    @Deprecated("Please use count(builder: WhereBuilder) instead.", level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("count(builder)")
    )
    override fun selectCount(builder: WhereBuilder): Long {
        return count(builder)
    }

    /**
     * Count the number of query results.
     * 简体中文：统计查询的结果数量。
     */
    @Deprecated("Please use count(builder: QueryBuilder) instead.", level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("count(builder)")
    )
    override fun selectCount(builder: QueryBuilder): Long {
        return count(builder)
    }

    /**
     * Count the number of query results.
     * 简体中文：统计查询的结果数量。
     */
    override fun count(): Long {
        return countInternal()
    }

    /**
     * Count the number of query results.
     * 简体中文：统计查询的结果数量。
     */
    private fun countInternal() : Long {
        var count: Long = 0
        try {
            val tableName: String = TableManager.getTableName(beanClass)
            val cursor = database.rawQuery("SELECT COUNT(*) FROM $tableName", null)
            if (cursor != null) {
                cursor.moveToFirst()
                count = cursor.getLong(0)
                cursor.close()
            }
        } catch (e: Exception) {
            OrmLog.d("select count(*) result is zero")
        }
        return count
    }

    /**
     * Count the number of query results.
     * 简体中文：统计查询的结果数量。
     */
    override fun count(builder: WhereBuilder): Long {
        return count(QueryBuilder.create().where(builder))
    }

    /**
     * Count the number of query results.
     * 简体中文：统计查询的结果数量。
     */
    override fun count(builder: QueryBuilder): Long {
        return countInternal(builder)
    }

    /**
     * Query the total count of records.
     * 简体中文：查询数据总数。
     */
    override fun countAsync(listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.Count, this, null,
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Query the total count of records that matches the conditions.
     * 简体中文：查询符合条件的数据总数。
     */
    override fun countAsync(builder: WhereBuilder, listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.WhereCount, this, builder,
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Query the total count of records that matches the conditions.
     * 简体中文：查询符合条件的数据总数。
     */
    override fun countAsync(builder: QueryBuilder, listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.QueryCount, this, builder,
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Count the number of query results.
     * 简体中文：统计查询的结果数量。
     */
    private fun countInternal(builder: QueryBuilder) : Long {
        var count: Long = 0
        try {
            val tableName: String = TableManager.getTableName(beanClass)
            val sql: String = builder.build()
            val cursor = database.rawQuery("SELECT COUNT(*) FROM $tableName$sql",
                builder.getWhereBuilder().selectionArgs)
            if (cursor != null) {
                cursor.moveToFirst()
                count = cursor.getLong(0)
                cursor.close()
            }
        } catch (e: Exception) {
            OrmLog.d("select count(*) result is zero")
        }
        return count
    }

    /**
     * Add a new column to the table.
     * 简体中文：给表添加新列。
     */
    override fun addColumn(fieldName: String): Boolean {
        val tableName = TableManager.getTableName(beanClass)
        try {
            val field = beanClass.getDeclaredField(fieldName)
            field.isAccessible = true
            val ignore = field.getAnnotation(Ignore::class.java)
            if (ignore != null || field.modifiers and Modifier.STATIC != 0) {
                return false
            }
            try {
                val sql = (TableManager.ALTER_TABLE + TableManager.SPACE + tableName
                        + TableManager.SPACE + TableManager.ADD_COLUMN + TableManager.SPACE
                        + TableManager.createColumnBuilder(field).build() + TableManager.SEMICOLON)
                OrmLog.d(sql)
                database.execSQL(sql)
            } catch (e: SQLException) {
                e.message?.let { OrmLog.i(it) }
            }
        } catch (e: NoSuchFieldException) {
            return false
        }
        return true
    }

    /**
     * Add a new column to the table.
     * 简体中文：向表中添加新列。
     */
    override fun addColumnAsync(fieldName: String, listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.AddColumn, this, fieldName,
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Rename a column in a table.
     * 简体中文：重命名表中的列。
     */
    override fun renameColumn(fieldName: String, oldColumnName: String): Boolean {
        val tableName = TableManager.getTableName(beanClass)
        try {
            val field = beanClass.getDeclaredField(fieldName)
            field.isAccessible = true
            val ignore = field.getAnnotation(Ignore::class.java)
            if (ignore != null || field.modifiers and Modifier.STATIC != 0) {
                return false
            }
            try {
                val sql = (TableManager.ALTER_TABLE + TableManager.SPACE + tableName
                        + TableManager.SPACE + TableManager.RENAME_COLUMN + TableManager.SPACE
                        + oldColumnName  + TableManager.TO + TableManager.getColumnName(field)
                        + TableManager.SEMICOLON)
                OrmLog.d(sql)
                database.execSQL(sql)
            } catch (e: SQLException) {
                e.message?.let { OrmLog.i(it) }
            }
        } catch (e: NoSuchFieldException) {
            return false
        } catch (e: SQLException) {
            e.message?.let { OrmLog.i(it) }
            return false
        }
        return true
    }

    /**
     * Rename a column in the table.
     * 简体中文：重命名表中的列。
     */
    override fun renameColumnAsync(
        fieldName: String,
        oldColumnName: String,
        listener: OrmTaskListener<T>?
    ) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.RenameColumn, this,
            Callable {
                     renameColumn(fieldName, oldColumnName)
            },
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Rename the table.
     * 简体中文：重命名表。
     */
    override fun renameTable(oldTableName: String): Boolean {
        val tableName = TableManager.getTableName(beanClass)
        try {
            val sql = (TableManager.ALTER_TABLE + TableManager.SPACE + oldTableName
                    + TableManager.SPACE + TableManager.RENAME_TO + TableManager.SPACE
                    + tableName + TableManager.SEMICOLON)
            OrmLog.d(sql)
            database.execSQL(sql)
        } catch (e: SQLException) {
            e.message?.let { OrmLog.i(it) }
            return false
        }
        return true
    }

    /**
     * Rename the table.
     * 简体中文：重命名表。
     */
    override fun renameTableAsync(oldTableName: String, listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.RenameTable, this, oldTableName,
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Delete a table, including its data and structure.
     * 简体中文：删除表，包括表数据和表结构。
     */
    override fun drop(): Boolean {
        try {
            val tableName = TableManager.getTableName(beanClass)
            val sql = TableManager.DROP_TABLE + TableManager.SPACE + tableName
            OrmLog.d(sql)
            database.execSQL(sql)
            DaoFactory.removeDao(beanClass)
        } catch (e: SQLException) {
            e.message?.let { OrmLog.i(it) }
            return false
        }
        return true
    }

    /**
     * Drop the table.
     * 简体中文：删除表。
     */
    override fun dropAsync(listener: OrmTaskListener<T>?) {
        executor.listener = listener
        executor.enqueue(OrmTask(OrmTask.Type.Drop, this, null,
            OrmTask.FLAG_TRACK_CREATOR_STACKTRACE))
    }

    /**
     * Collect query results.
     * 简体中文：收集查询结果。
     */
    private fun getResult(cursor: Cursor): List<T> {
        val result: MutableList<T> = arrayListOf()
        while (cursor.moveToNext()) {
            try {
                val bean = createResult(cursor)
                if (bean != null) {
                    result.add(bean)
                }
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
        }
        return result
    }

    /**
     * Create an instance of an OrmTable implementation class.
     * 简体中文：创建一个OrmTable实现类的实例。
     */
    private fun <T : OrmTable> newOrmTableInstance(clazz: Class<T>): T? {
        val constructors = clazz.declaredConstructors
        for (c in constructors) {
            c.isAccessible = true
            val cls = c.parameterTypes
            if (cls.isEmpty()) {
                try {
                    return c.newInstance() as T
                } catch (e: InstantiationException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                }
            } else {
                val objs = arrayOfNulls<Any>(cls.size)
                for (i in cls.indices) {
                    objs[i] = getPrimitiveDefaultValue(cls[i])
                }
                try {
                    return c.newInstance(*objs) as T
                } catch (e: InstantiationException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    /**
     * Gets the default value of a primitive data type.
     * 简体中文：获取基本数据类型的默认值。
     */
    private fun getPrimitiveDefaultValue(clazz: Class<*>): Any? {
        return if (clazz.isPrimitive) {
            if (clazz == Boolean::class.javaPrimitiveType) false else 0
        } else null
    }

    /**
     * Gets the name of the primary key property.
     * 简体中文：获取主键的属性名称。
     */
    private fun getPrimaryKeyFieldName(bean: T): String {
        val fields = bean.javaClass.declaredFields
        for (field in fields) {
            field.isAccessible = true
            if (isOrmTableField(field)) {
                continue
            }
            val id: Id? = field.getAnnotation(Id::class.java)
            if (id != null) {
                return field.name
            }
            val primaryKey: PrimaryKey? = field.getAnnotation(PrimaryKey::class.java)
            if (primaryKey != null) {
                return field.name
            }
        }
        return ""
    }

    /**
     * Convert the data stored in the database into data entity objects.
     * 简体中文：将数据库中保存的数据转换成数据实体对象。
     */
    @Throws(IllegalAccessException::class, ClassNotFoundException::class)
    private fun createResult(cursor: Cursor): T? {
        val bean: T = newOrmTableInstance(beanClass) ?: throw OrmResultCreationException("Failed" +
                " to create ${beanClass.name} instance.")
        val fields = beanClass.declaredFields
        for (field in fields) {
            field.isAccessible = true
            if (isOrmTableField(field)) {
                continue
            }
            var columnName: String
            val id: Id? = field.getAnnotation(Id::class.java)
            val column: Column? = field.getAnnotation(Column::class.java)
            (if (id != null) {
                OrmTable.INDEX_ID
            } else column?.value ?: TableManager.generateColumnName(field.name)).also { columnName = it }
            val convert: Convert? = field.getAnnotation(Convert::class.java)
            val columnIndex = cursor.getColumnIndex(columnName)
            if (columnIndex != -1) {
                val fieldType: Class<*> = convert?.columnType?.java ?: field.type
                if (isAssignableFromCharSequence(fieldType)) {
                    if (convert != null) {
                        val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                        val propertyConverter: PropertyConverter<Any, String> = Proxy
                            .newProxyInstance(converter.classLoader,
                                converter.interfaces, PropertyHandler(converter))
                                as PropertyConverter<Any, String>
                        val value: Any? = propertyConverter.convertToEntityProperty(
                            cursor.getString(columnIndex))
                        field[bean] = value
                    } else {
                        field[bean] = cursor.getString(columnIndex)
                    }
                } else if (isAssignableFromBoolean(fieldType)) {
                    if (convert != null) {
                        val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                        val propertyConverter: PropertyConverter<Any, Boolean> = Proxy
                            .newProxyInstance(converter.classLoader,
                                converter.interfaces, PropertyHandler(converter))
                                as PropertyConverter<Any, Boolean>
                        val value: Any? = propertyConverter.convertToEntityProperty(
                            cursor.getInt(columnIndex) == 1)
                        field[bean] = value
                    } else {
                        field[bean] = (cursor.getInt(columnIndex) == 1)
                    }
                } else if (isAssignableFromLong(fieldType)) {
                    if (convert != null) {
                        val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                        val propertyConverter: PropertyConverter<Any, Long> = Proxy
                            .newProxyInstance(converter.classLoader,
                                converter.interfaces, PropertyHandler(converter))
                                as PropertyConverter<Any, Long>
                        val value: Any? = propertyConverter.convertToEntityProperty(
                            cursor.getLong(columnIndex))
                        field[bean] = value
                    } else {
                        field[bean] = cursor.getLong(columnIndex)
                    }
                } else if (isAssignableFromInteger(fieldType)) {
                    if (convert != null) {
                        val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                        val propertyConverter: PropertyConverter<Any, Int> = Proxy
                            .newProxyInstance(converter.classLoader,
                                converter.interfaces, PropertyHandler(converter))
                                as PropertyConverter<Any, Int>
                        val value: Any? = propertyConverter.convertToEntityProperty(
                            cursor.getInt(columnIndex))
                        field[bean] = value
                    } else {
                        field[bean] = cursor.getInt(columnIndex)
                    }
                } else if (isAssignableFromShort(fieldType)) {
                    if (convert != null) {
                        val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                        val propertyConverter: PropertyConverter<Any, Short> = Proxy
                            .newProxyInstance(converter.classLoader,
                                converter.interfaces, PropertyHandler(converter))
                                as PropertyConverter<Any, Short>
                        val value: Any? = propertyConverter.convertToEntityProperty(
                            cursor.getShort(columnIndex))
                        field[bean] = value
                    } else {
                        field[bean] = cursor.getShort(columnIndex)
                    }
                } else if (isAssignableFromByte(fieldType)) {
                    if (convert != null) {
                        val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                        val propertyConverter: PropertyConverter<Any, Byte> = Proxy
                            .newProxyInstance(converter.classLoader,
                            converter.interfaces, PropertyHandler(converter))
                                as PropertyConverter<Any, Byte>
                        val value: Any? = propertyConverter.convertToEntityProperty(
                            cursor.getShort(columnIndex).toByte())
                        field[bean] = value
                    } else {
                        field[bean] = cursor.getShort(columnIndex).toByte()
                    }
                } else if (isAssignableFromDouble(fieldType)) {
                    if (convert != null) {
                        val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                        val propertyConverter: PropertyConverter<Any, Double> = Proxy
                            .newProxyInstance(converter.classLoader,
                                converter.interfaces, PropertyHandler(converter))
                                as PropertyConverter<Any, Double>
                        val value: Any? = propertyConverter.convertToEntityProperty(
                            cursor.getDouble(columnIndex))
                        field[bean] = value
                    } else {
                        field[bean] = cursor.getDouble(columnIndex)
                    }
                } else if (isAssignableFromFloat(fieldType)) {
                    if (convert != null) {
                        val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                        val propertyConverter: PropertyConverter<Any, Float> = Proxy
                            .newProxyInstance(converter.classLoader,
                                converter.interfaces, PropertyHandler(converter))
                                as PropertyConverter<Any, Float>
                        val value: Any? = propertyConverter.convertToEntityProperty(
                            cursor.getFloat(columnIndex))
                        field[bean] = value
                    } else {
                        field[bean] = cursor.getFloat(columnIndex)
                    }
                } else if (isAssignableFromCharacter(fieldType)) {
                    if (convert != null) {
                        val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                        val propertyConverter: PropertyConverter<Any, Char> = Proxy
                            .newProxyInstance(converter.classLoader,
                                converter.interfaces, PropertyHandler(converter))
                                as PropertyConverter<Any, Char>
                        val value: Any? = propertyConverter.convertToEntityProperty(
                            cursor.getString(columnIndex).first())
                        field[bean] = value
                    } else {
                        field[bean] = cursor.getString(columnIndex).first()
                    }
                } else if (isAssignableFromClass(fieldType)) {
                    if (convert != null) {
                        val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                        val propertyConverter: PropertyConverter<Any, Class<*>> = Proxy
                            .newProxyInstance(converter.classLoader,
                                converter.interfaces, PropertyHandler(converter))
                                as PropertyConverter<Any, Class<*>>
                        val value: Any? = propertyConverter.convertToEntityProperty(
                            Class.forName(cursor.getString(columnIndex)))
                        field[bean] = value
                    } else {
                        field[bean] = Class.forName(cursor.getString(columnIndex))
                    }
                } else {
                    field[bean] = cursor.getBlob(columnIndex)
                }
            }
        }
        return bean
    }

    internal class PropertyHandler(clazz: Class<out PropertyConverter<*, *>>) : InvocationHandler {

        private val clazz: Class<out PropertyConverter<*, *>> = clazz

        private fun <T> newInstance(clazz: Class<T>): T? {
            val constructors = clazz.declaredConstructors
            for (c in constructors) {
                c.isAccessible = true
                val cls = c.parameterTypes
                if (cls.isEmpty()) {
                    try {
                        return c.newInstance() as T
                    } catch (e: InstantiationException) {
                        e.printStackTrace()
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    } catch (e: InvocationTargetException) {
                        e.printStackTrace()
                    }
                } else {
                    val objs = arrayOfNulls<Any>(cls.size)
                    for (i in cls.indices) {
                        objs[i] = getPrimitiveDefaultValue(cls[i])
                    }
                    try {
                        return c.newInstance(*objs) as T
                    } catch (e: InstantiationException) {
                        e.printStackTrace()
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    } catch (e: InvocationTargetException) {
                        e.printStackTrace()
                    }
                }
            }
            return null
        }

        private fun getPrimitiveDefaultValue(clazz: Class<*>): Any? {
            return if (clazz.isPrimitive) {
                if (clazz == Boolean::class.javaPrimitiveType) false else 0
            } else null
        }

        @Throws(Throwable::class)
        override fun invoke(proxy: Any?, method: Method, args: Array<out Any>): Any? {
            return method.invoke(newInstance(clazz), *args)
        }
    }

    /**
     * Use it to perform operations in a transaction, similar to multi-table transactions of
     * [dora.db.Transaction].
     * 简体中文：用它在事务中执行操作，同[dora.db.Transaction]的多表事务。
     *
     * @see dora.db.Transaction
     */
    fun runInTransaction(block:() -> T) {
        try {
            // Begin the transaction.
            // 简体中文：开始事务
            database.beginTransaction()
            // Execute the transaction operation.
            // 简体中文：执行事务操作
            block()
            // Set the flag indicating that all operations were executed successfully.
            // 简体中文：设置所有操作执行成功的标志位
            database.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            // End the transaction.
            // 简体中文：结束事务
            database.endTransaction()
        }
    }
}