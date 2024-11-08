package dora.db.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import dora.db.Orm
import dora.db.OrmLog
import dora.db.builder.Condition
import dora.db.builder.QueryBuilder
import dora.db.builder.WhereBuilder
import dora.db.constraint.AssignType
import dora.db.constraint.Id
import dora.db.constraint.PrimaryKey
import dora.db.converter.PropertyConverter
import dora.db.exception.OrmResultCreationException
import dora.db.exception.UnsupportedDataTypeException
import dora.db.table.*
import java.lang.reflect.*
import java.util.*

class OrmDao<T : OrmTable> internal constructor(private val beanClass: Class<T>) : Dao<T> {

    private val database: SQLiteDatabase = Orm.getDB()

    private fun isAssignableFromBoolean(fieldType: Class<*>): Boolean {
        return Boolean::class.javaPrimitiveType!!.isAssignableFrom(fieldType) ||
                Boolean::class.java.isAssignableFrom(fieldType)
    }

    private fun isAssignableFromByte(fieldType: Class<*>): Boolean {
        return Byte::class.javaPrimitiveType!!.isAssignableFrom(fieldType) ||
                Byte::class.java.isAssignableFrom(fieldType)
    }

    private fun isAssignableFromShort(fieldType: Class<*>): Boolean {
        return Short::class.javaPrimitiveType!!.isAssignableFrom(fieldType) ||
                Short::class.java.isAssignableFrom(fieldType)
    }

    private fun isAssignableFromInteger(fieldType: Class<*>): Boolean {
        return Int::class.javaPrimitiveType!!.isAssignableFrom(fieldType) ||
                Int::class.java.isAssignableFrom(fieldType)
    }

    private fun isAssignableFromLong(fieldType: Class<*>): Boolean {
        return Long::class.javaPrimitiveType!!.isAssignableFrom(fieldType) ||
                Long::class.java.isAssignableFrom(fieldType)
    }

    private fun isAssignableFromFloat(fieldType: Class<*>): Boolean {
        return Float::class.javaPrimitiveType!!.isAssignableFrom(fieldType) ||
                Float::class.java.isAssignableFrom(fieldType)
    }

    private fun isAssignableFromDouble(fieldType: Class<*>): Boolean {
        return Double::class.javaPrimitiveType!!.isAssignableFrom(fieldType) ||
                Double::class.java.isAssignableFrom(fieldType)
    }

    private fun isAssignableFromCharacter(fieldType: Class<*>): Boolean {
        return Char::class.javaPrimitiveType!!.isAssignableFrom(fieldType) ||
                Char::class.java.isAssignableFrom(fieldType)
    }

    private fun isAssignableFromCharSequence(fieldType: Class<*>): Boolean {
        return CharSequence::class.java.isAssignableFrom(fieldType) ||
                String::class.java.isAssignableFrom(fieldType)
    }

    private fun isAssignableFromClass(fieldType: Class<*>): Boolean {
        return Class::class.java.isAssignableFrom(fieldType)
    }

    private fun isOrmTableField(field: Field) : Boolean {
        return field.name.equals("isUpgradeRecreated") || field.name.equals("migrations")
    }

    private fun convertBooleanToInt(boolean: Boolean) : Int {
        return if (boolean) 1 else 0
    }

    private fun getContentValues(bean: T): ContentValues {
        val values = ContentValues()
        val fields = beanClass.declaredFields
        for (field in fields) {
            field.isAccessible = true
            val ignore: Ignore? = field.getAnnotation(Ignore::class.java)
            val id: Id? = field.getAnnotation(Id::class.java)
            val column: Column? = field.getAnnotation(Column::class.java)
            val primaryKey: PrimaryKey? = field.getAnnotation(PrimaryKey::class.java)
            val convert: Convert? = field.getAnnotation(Convert::class.java)
            if (ignore != null || field.modifiers and Modifier.STATIC != 0) {
                continue
            }
            if (id != null) {
                continue
            }
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
                    val propertyConverter: PropertyConverter<Any, String> = Proxy.newProxyInstance(converter.classLoader,
                            converter.interfaces, PropertyHandler(converter)) as PropertyConverter<Any, String>
                    value?.let {
                        values.put(columnName, propertyConverter.convertToDatabaseValue(it))
                    }
                } else {
                    values.put(columnName, field[bean]?.toString() ?: "")
                }
            } else if (isAssignableFromBoolean(fieldType)) {
                if (convert != null) {
                    val value = field[bean]
                    val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                    val propertyConverter: PropertyConverter<Any, Boolean> = Proxy.newProxyInstance(converter.classLoader,
                            converter.interfaces, PropertyHandler(converter)) as PropertyConverter<Any, Boolean>
                    value?.let {
                        values.put(columnName, convertBooleanToInt(propertyConverter.convertToDatabaseValue(it) as Boolean))
                    }
                } else {
                    values.put(columnName, convertBooleanToInt(field.getBoolean(bean)))
                }
            } else if (isAssignableFromByte(fieldType)) {
                if (convert != null) {
                    val value = field[bean]
                    val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                    val propertyConverter: PropertyConverter<Any, Byte> = Proxy.newProxyInstance(converter.classLoader,
                            converter.interfaces, PropertyHandler(converter)) as PropertyConverter<Any, Byte>
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
                    val propertyConverter: PropertyConverter<Any, Short> = Proxy.newProxyInstance(converter.classLoader,
                            converter.interfaces, PropertyHandler(converter)) as PropertyConverter<Any, Short>
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
                    val propertyConverter: PropertyConverter<Any, Int> = Proxy.newProxyInstance(converter.classLoader,
                            converter.interfaces, PropertyHandler(converter)) as PropertyConverter<Any, Int>
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
                    val propertyConverter: PropertyConverter<Any, Long> = Proxy.newProxyInstance(converter.classLoader,
                            converter.interfaces, PropertyHandler(converter)) as PropertyConverter<Any, Long>
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
                    val propertyConverter: PropertyConverter<Any, Float> = Proxy.newProxyInstance(converter.classLoader,
                            converter.interfaces, PropertyHandler(converter)) as PropertyConverter<Any, Float>
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
                    val propertyConverter: PropertyConverter<Any, Double> = Proxy.newProxyInstance(converter.classLoader,
                            converter.interfaces, PropertyHandler(converter)) as PropertyConverter<Any, Double>
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
                    val propertyConverter: PropertyConverter<Any, Char> = Proxy.newProxyInstance(converter.classLoader,
                        converter.interfaces, PropertyHandler(converter)) as PropertyConverter<Any, Char>
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
                    val propertyConverter: PropertyConverter<Any, Class<*>> = Proxy.newProxyInstance(converter.classLoader,
                        converter.interfaces, PropertyHandler(converter)) as PropertyConverter<Any, Class<*>>
                    value?.let {
                        values.put(columnName, propertyConverter.convertToDatabaseValue(it)?.name ?: "")
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

    override fun insert(bean: T): Boolean {
        return insertInternal(bean, database)
    }

    override fun insert(beans: List<T>): Boolean {
        return insertInternal(beans, database)
    }

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

    private fun insertInternal(bean: T, db: SQLiteDatabase): Boolean {
        val tableName: String = TableManager.getTableName(beanClass)
        val contentValues = getContentValues(bean)
        return db.insert(tableName, columnHack, contentValues) > 0
    }

    override fun delete(builder: WhereBuilder): Boolean {
        return deleteInternal(builder, database)
    }

    override fun delete(bean: T): Boolean {
        val field = bean.javaClass.getDeclaredField(getPrimaryKeyFieldName(bean))
        field.isAccessible = true
        val name = TableManager.getColumnName(field)
        val value = field.get(bean)?.toString()
        return deleteInternal(WhereBuilder.create(Condition("$name=?", arrayOf(value))), database)
    }

    override fun deleteAll(): Boolean {
        return deleteAllInternal(database)
    }

    private fun deleteAllInternal(db: SQLiteDatabase): Boolean {
        val tableName: String = TableManager.getTableName(beanClass)
        return db.delete(tableName, null, null) > 0
    }

    private fun deleteInternal(builder: WhereBuilder, db: SQLiteDatabase): Boolean {
        val tableName: String = TableManager.getTableName(beanClass)
        return db.delete(tableName, builder.selection, builder.selectionArgs) > 0
    }

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

    override fun insertOrUpdate(builder: WhereBuilder, newBean: T): Boolean {
        return insertOrUpdateInternal(builder, newBean)
    }

    private fun insertOrUpdateInternal(bean: T): Boolean {
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

    override fun insertOrUpdate(bean: T): Boolean {
        return insertOrUpdateInternal(bean)
    }

    override fun update(builder: WhereBuilder, newBean: T): Boolean {
        return updateInternal(builder, newBean, database)
    }

    override fun update(bean: T): Boolean {
        val field = bean.javaClass.getDeclaredField(getPrimaryKeyFieldName(bean))
        field.isAccessible = true
        val name = TableManager.getColumnName(field)
        val value = field.get(bean)?.toString()
        return updateInternal(WhereBuilder.create(Condition("$name=?", arrayOf(value))),
                bean, database)
    }

    private fun updateAllInternal(newBean: T, db: SQLiteDatabase): Boolean {
        val tableName: String = TableManager.getTableName(beanClass)
        val contentValues = getContentValues(newBean)
        return db.update(tableName, contentValues, null, null) > 0
    }

    private fun updateInternal(builder: WhereBuilder, newBean: T, db: SQLiteDatabase): Boolean {
        val tableName: String = TableManager.getTableName(beanClass)
        val contentValues = getContentValues(newBean)
        return db.update(tableName, contentValues, builder.selection, builder.selectionArgs) > 0
    }

    override fun selectAll(): List<T> {
        return selectAllInternal()
    }

    private fun selectAllInternal(): List<T> {
        val tableName: String = TableManager.getTableName(beanClass)
        val cursor = database.query(tableName, null, null, null, null, null, null)
        return getResult(cursor)
    }

    override fun select(builder: WhereBuilder): List<T> {
        return select(QueryBuilder.create().where(builder))
    }

    override fun select(builder: QueryBuilder): List<T> {
        return selectInternal(builder)
    }

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

    override fun selectOne(): T? {
        return selectOneInternal()
    }

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

    override fun selectOne(builder: WhereBuilder): T? {
        return selectOne(QueryBuilder.create().where(builder))
    }

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
        val cursor = database.query(tableName, columns, selection, selectionArgs, group, having, order, limit)
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

    override fun selectOne(builder: QueryBuilder): T? {
        return selectOneInternal(builder)
    }

    @Deprecated("Please use count() instead.", level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("count()")
    )
    override fun selectCount(): Long {
        return count()
    }

    @Deprecated("Please use count(builder: WhereBuilder) instead.", level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("count(builder)")
    )
    override fun selectCount(builder: WhereBuilder): Long {
        return count(builder)
    }

    @Deprecated("Please use count(builder: QueryBuilder) instead.", level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith("count(builder)")
    )
    override fun selectCount(builder: QueryBuilder): Long {
        return count(builder)
    }

    override fun count(): Long {
        return countInternal()
    }

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

    override fun count(builder: WhereBuilder): Long {
        return count(QueryBuilder.create().where(builder))
    }

    override fun count(builder: QueryBuilder): Long {
        return countInternal(builder)
    }

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
        }
        return true
    }

    override fun drop(): Boolean {
        try {
            val tableName = TableManager.getTableName(beanClass)
            val sql = TableManager.DROP_TABLE + TableManager.SPACE + tableName
            OrmLog.d(sql)
            database.execSQL(sql)
            DaoFactory.removeDao(beanClass)
        } catch (e: Exception) {
            return false
        }
        return true
    }

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

    private fun getPrimitiveDefaultValue(clazz: Class<*>): Any? {
        return if (clazz.isPrimitive) {
            if (clazz == Boolean::class.javaPrimitiveType) false else 0
        } else null
    }

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

    @Throws(IllegalAccessException::class, ClassNotFoundException::class)
    private fun createResult(cursor: Cursor): T? {
        val bean: T = newOrmTableInstance(beanClass) ?: throw OrmResultCreationException("Failed to create ${beanClass.name} instance.")
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
                        val propertyConverter: PropertyConverter<Any, String> = Proxy.newProxyInstance(converter.classLoader,
                                converter.interfaces, PropertyHandler(converter)) as PropertyConverter<Any, String>
                        val value: Any? = propertyConverter.convertToEntityProperty(cursor.getString(columnIndex))
                        field[bean] = value
                    } else {
                        field[bean] = cursor.getString(columnIndex)
                    }
                } else if (isAssignableFromBoolean(fieldType)) {
                    if (convert != null) {
                        val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                        val propertyConverter: PropertyConverter<Any, Boolean> = Proxy.newProxyInstance(converter.classLoader,
                                converter.interfaces, PropertyHandler(converter)) as PropertyConverter<Any, Boolean>
                        val value: Any? = propertyConverter.convertToEntityProperty(cursor.getInt(columnIndex) == 1)
                        field[bean] = value
                    } else {
                        field[bean] = (cursor.getInt(columnIndex) == 1)
                    }
                } else if (isAssignableFromLong(fieldType)) {
                    if (convert != null) {
                        val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                        val propertyConverter: PropertyConverter<Any, Long> = Proxy.newProxyInstance(converter.classLoader,
                                converter.interfaces, PropertyHandler(converter)) as PropertyConverter<Any, Long>
                        val value: Any? = propertyConverter.convertToEntityProperty(cursor.getLong(columnIndex))
                        field[bean] = value
                    } else {
                        field[bean] = cursor.getLong(columnIndex)
                    }
                } else if (isAssignableFromInteger(fieldType)) {
                    if (convert != null) {
                        val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                        val propertyConverter: PropertyConverter<Any, Int> = Proxy.newProxyInstance(converter.classLoader,
                                converter.interfaces, PropertyHandler(converter)) as PropertyConverter<Any, Int>
                        val value: Any? = propertyConverter.convertToEntityProperty(cursor.getInt(columnIndex))
                        field[bean] = value
                    } else {
                        field[bean] = cursor.getInt(columnIndex)
                    }
                } else if (isAssignableFromShort(fieldType)) {
                    if (convert != null) {
                        val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                        val propertyConverter: PropertyConverter<Any, Short> = Proxy.newProxyInstance(converter.classLoader,
                                converter.interfaces, PropertyHandler(converter)) as PropertyConverter<Any, Short>
                        val value: Any? = propertyConverter.convertToEntityProperty(cursor.getShort(columnIndex))
                        field[bean] = value
                    } else {
                        field[bean] = cursor.getShort(columnIndex)
                    }
                } else if (isAssignableFromByte(fieldType)) {
                    if (convert != null) {
                        val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                        val propertyConverter: PropertyConverter<Any, Byte> = Proxy.newProxyInstance(converter.classLoader,
                            converter.interfaces, PropertyHandler(converter)) as PropertyConverter<Any, Byte>
                        val value: Any? = propertyConverter.convertToEntityProperty(cursor.getShort(columnIndex).toByte())
                        field[bean] = value
                    } else {
                        field[bean] = cursor.getShort(columnIndex).toByte()
                    }
                } else if (isAssignableFromDouble(fieldType)) {
                    if (convert != null) {
                        val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                        val propertyConverter: PropertyConverter<Any, Double> = Proxy.newProxyInstance(converter.classLoader,
                                converter.interfaces, PropertyHandler(converter)) as PropertyConverter<Any, Double>
                        val value: Any? = propertyConverter.convertToEntityProperty(cursor.getDouble(columnIndex))
                        field[bean] = value
                    } else {
                        field[bean] = cursor.getDouble(columnIndex)
                    }
                } else if (isAssignableFromFloat(fieldType)) {
                    if (convert != null) {
                        val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                        val propertyConverter: PropertyConverter<Any, Float> = Proxy.newProxyInstance(converter.classLoader,
                                converter.interfaces, PropertyHandler(converter)) as PropertyConverter<Any, Float>
                        val value: Any? = propertyConverter.convertToEntityProperty(cursor.getFloat(columnIndex))
                        field[bean] = value
                    } else {
                        field[bean] = cursor.getFloat(columnIndex)
                    }
                } else if (isAssignableFromCharacter(fieldType)) {
                    if (convert != null) {
                        val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                        val propertyConverter: PropertyConverter<Any, Char> = Proxy.newProxyInstance(converter.classLoader,
                                converter.interfaces, PropertyHandler(converter)) as PropertyConverter<Any, Char>
                        val value: Any? = propertyConverter.convertToEntityProperty(cursor.getString(columnIndex).first())
                        field[bean] = value
                    } else {
                        field[bean] = cursor.getString(columnIndex).first()
                    }
                } else if (isAssignableFromClass(fieldType)) {
                    if (convert != null) {
                        val converter: Class<out PropertyConverter<*, *>> = convert.converter.java
                        val propertyConverter: PropertyConverter<Any, Class<*>> = Proxy.newProxyInstance(converter.classLoader,
                                converter.interfaces, PropertyHandler(converter)) as PropertyConverter<Any, Class<*>>
                        val value: Any? = propertyConverter.convertToEntityProperty(Class.forName(cursor.getString(columnIndex)))
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