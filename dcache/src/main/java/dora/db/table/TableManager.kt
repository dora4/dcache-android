package dora.db.table

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import dora.db.Orm
import dora.db.OrmLog
import dora.db.Transaction
import dora.db.constraint.*
import dora.db.dao.DaoFactory.removeDao
import dora.db.exception.ConstraintException
import dora.db.type.DataType
import dora.db.type.BooleanType
import dora.db.type.ByteArrayType
import dora.db.type.ByteType
import dora.db.type.CharType
import dora.db.type.ClassType
import dora.db.type.DoubleType
import dora.db.type.FloatType
import dora.db.type.IntType
import dora.db.type.LongType
import dora.db.type.ShortType
import dora.db.type.SqlType
import dora.db.type.StringType
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.*

object TableManager {

    private val A = 'A'
    private val Z = 'Z'
    private val CREATE_TABLE = "CREATE TABLE"
    private val ALTER_TABLE = "ALTER TABLE"
    private val DROP_TABLE = "DROP TABLE"
    private val IF_NOT_EXISTS = "IF NOT EXISTS"
    private val ADD_COLUMN = "ADD COLUMN"
    private val AUTO_INCREMENT = "AUTOINCREMENT"
    private val SPACE = " "
    private val SINGLE_QUOTES = "\'"
    private val UNIQUE = "UNIQUE"
    private val DEFAULT = "DEFAULT"
    private val CHECK = "CHECK"
    private val NOT_NULL = "NOT NULL"
    private val PRIMARY_KEY = "PRIMARY KEY"
    private val LEFT_PARENTHESIS = "("
    private val RIGHT_PARENTHESIS = ")"
    private val COMMA = ","
    private val SEMICOLON = ";"
    private val UNDERLINE = "_"
    private val TABLE_NAME_HEADER = "t$UNDERLINE"

    fun <T : OrmTable> getTableName(tableClass: Class<T>): String {
        val table = tableClass.getAnnotation(Table::class.java)
        val tableName: String
        tableName = if (table != null) {
            table.value
        } else {
            val className = tableClass.simpleName
            generateTableName(className)
        }
        return tableName
    }

    fun getColumnName(field: Field): String {
        val columnName: String
        val id = field.getAnnotation(Id::class.java)
        val column = field.getAnnotation(Column::class.java)
        columnName = when {
            id != null -> {
                OrmTable.INDEX_ID
            }
            column != null -> {
                column.value
            }
            else -> {
                val fieldName = field.name
                generateColumnName(fieldName)
            }
        }
        return columnName
    }

    fun generateTableName(className: String): String {
        val sb = StringBuilder()
        for (i in className.indices) {
            if (className[i] in A..Z && i != 0) {
                sb.append(UNDERLINE)
            }
            sb.append(className[i].toString().toLowerCase(Locale.ENGLISH))
        }
        return TABLE_NAME_HEADER + sb.toString().toLowerCase()
    }

    fun generateColumnName(fieldName: String): String {
        val sb = StringBuilder()
        for (i in fieldName.indices) {
            if (fieldName[i] in A..Z && i != 0) {
                sb.append(UNDERLINE)
            }
            sb.append(fieldName[i].toString().toLowerCase(Locale.ENGLISH))
        }
        return sb.toString().toLowerCase()
    }

    private val declaredDataTypes: List<DataType>
        get() {
            val dataTypes: MutableList<DataType> = arrayListOf()
            dataTypes.add(BooleanType.INSTANCE)
            dataTypes.add(ByteType.INSTANCE)
            dataTypes.add(ShortType.INSTANCE)
            dataTypes.add(IntType.INSTANCE)
            dataTypes.add(LongType.INSTANCE)
            dataTypes.add(FloatType.INSTANCE)
            dataTypes.add(DoubleType.INSTANCE)
            dataTypes.add(CharType.INSTANCE)
            dataTypes.add(StringType.INSTANCE)
            dataTypes.add(ClassType.INSTANCE)
            return dataTypes
        }

    private fun matchDataType(fieldType: Class<*>): DataType {
        val dataTypes: List<DataType> = declaredDataTypes
        for (dataType in dataTypes) {
            if (dataType.matches(fieldType)) {
                return dataType
            }
        }
        return ByteArrayType.INSTANCE
    }

    private fun <A : Annotation> checkColumnConstraint(field: Field, annotationType: Class<A>): Boolean {
        val annotation = field.getAnnotation(annotationType)
        return annotation != null
    }

    private fun <A : Annotation, V> getColumnConstraintValue(field: Field, annotationType: Class<A>,
                                                              valueType: Class<V>): V? {
        var value: V? = null
        val annotation = field.getAnnotation(annotationType)
        if (Default::class.java.isAssignableFrom(annotationType)) {
            value = (annotation as Default).value as V
        }
        if (Check::class.java.isAssignableFrom(annotationType)) {
            value = (annotation as Check).value as V
        }
        if (PrimaryKey::class.java.isAssignableFrom(annotationType)) {
            value = (annotation as PrimaryKey).value as V
        }
        return value
    }

    private class ColumnBuilder {

        private var builder: StringBuilder
        private var field: Field
        var isPrimaryKey = false

        constructor(field: Field) {
            this.field = field
            builder = StringBuilder()
        }

        constructor(str: String?, field: Field) {
            this.field = field
            builder = StringBuilder(str)
        }

        private fun append(str: String): ColumnBuilder {
            builder.append(str)
            return this
        }

        fun buildColumnUnique(): ColumnBuilder {
            if (checkColumnConstraint(field, Unique::class.java)) {
                builder.append(SPACE).append(UNIQUE)
            }
            return this
        }

        fun buildColumnDefault(): ColumnBuilder {
            if (checkColumnConstraint(field, Default::class.java)) {
                val value = getColumnConstraintValue(field, Default::class.java, String::class.java)!!
                try {
                    val number = value.toLong()
                    builder.append(SPACE).append(DEFAULT)
                            .append(SPACE).append(SINGLE_QUOTES).append(number).append(SINGLE_QUOTES)
                } catch (e: NumberFormatException) {
                    builder.append(SPACE).append(DEFAULT)
                            .append(SPACE).append(SINGLE_QUOTES).append(value).append(SINGLE_QUOTES)
                }
            }
            return this
        }

        fun buildColumnCheck(): ColumnBuilder {
            if (checkColumnConstraint(field, Check::class.java)) {
                val value = getColumnConstraintValue(field, Check::class.java, String::class.java)!!
                builder.append(SPACE).append(CHECK).append(LEFT_PARENTHESIS)
                        .append(value).append(RIGHT_PARENTHESIS)
            }
            return this
        }

        fun buildColumnNotNull(): ColumnBuilder {
            if (checkColumnConstraint(field, NotNull::class.java)) {
                builder.append(SPACE).append(NOT_NULL)
            }
            return this
        }

        fun buildColumnPrimaryKey(): ColumnBuilder {
            if (checkColumnConstraint(field, Id::class.java)) {
                isPrimaryKey = true
                builder.append(SPACE).append(PRIMARY_KEY).append(SPACE).append(AUTO_INCREMENT)
            } else if (checkColumnConstraint(field, PrimaryKey::class.java)) {
                isPrimaryKey = true
                builder.append(SPACE).append(PRIMARY_KEY)
                val assignType = getColumnConstraintValue(field, PrimaryKey::class.java,
                        AssignType::class.java)!!
                if (assignType == AssignType.BY_MYSELF) {
                } else if (assignType == AssignType.AUTO_INCREMENT) {
                    builder.append(SPACE).append(AUTO_INCREMENT)
                }
            }
            return this
        }

        fun build(): String {
            return builder.toString()
        }
    }

    private fun createColumnBuilder(field: Field): ColumnBuilder {
        val dataType: DataType = matchDataType(field.type)
        val sqlType: SqlType = dataType.sqlType
        var columnType: String = sqlType.name
        val convert: Convert? = field.getAnnotation(Convert::class.java)
        if (convert != null) {
            // 使用convert的columnType的值再次匹配
            columnType = matchDataType(convert.columnType.java).sqlType.name
        }
        val columnName = getColumnName(field)
        val fieldBuilder = ColumnBuilder(columnName + SPACE + columnType, field)
        fieldBuilder.buildColumnUnique()
                .buildColumnDefault()
                .buildColumnCheck()
                .buildColumnNotNull()
                .buildColumnPrimaryKey()
        return fieldBuilder
    }

    private fun <T : OrmTable> _createTable(tableClass: Class<T>, db: SQLiteDatabase) {
        val tableName = getTableName(tableClass)
        val fields = tableClass.declaredFields
        val sqlBuilder = StringBuilder(CREATE_TABLE + SPACE + IF_NOT_EXISTS + SPACE
                + tableName + LEFT_PARENTHESIS) //table header
        var hasPrimaryKey = false
        for (field in fields) {
            field.isAccessible = true
            val ignore = field.getAnnotation(Ignore::class.java)
            if (ignore != null || field.modifiers and Modifier.STATIC != 0) {
                continue
            }
            val fieldBuilder = createColumnBuilder(field)
            if (fieldBuilder.isPrimaryKey) {
                hasPrimaryKey = true
            }
            sqlBuilder.append(fieldBuilder.build()).append(COMMA)
        }
        if (!hasPrimaryKey) {
            throw ConstraintException("Lack valid primary key.")
        }
        try {
            val sql = sqlBuilder.deleteCharAt(sqlBuilder.length - 1).append(RIGHT_PARENTHESIS)
                    .append(SEMICOLON).toString()
            OrmLog.d(sql)
            db.execSQL(sql)
        } catch (e: SQLException) {
            e.message?.let { OrmLog.i(it) }
        }
        removeDao(tableClass)
    }

    private fun <T : OrmTable> _upgradeTable(tableClass: Class<T>, db: SQLiteDatabase) {
        val tableName = getTableName(tableClass)
        val fields = tableClass.declaredFields
        for (field in fields) {
            field.isAccessible = true
            val ignore = field.getAnnotation(Ignore::class.java)
            if (ignore != null || field.modifiers and Modifier.STATIC != 0) {
                continue
            }
            try {
                val sql = (ALTER_TABLE + SPACE + tableName + SPACE + ADD_COLUMN + SPACE
                        + createColumnBuilder(field).build() + SEMICOLON)
                OrmLog.d(sql)
                db.execSQL(sql)
            } catch (e: SQLException) {
                e.message?.let { OrmLog.i(it) }
            }
        }
        removeDao(tableClass)
    }

    private fun <T : OrmTable> _dropTable(tableClass: Class<T>, db: SQLiteDatabase) {
        val tableName = getTableName(tableClass)
        val sql = DROP_TABLE + SPACE + tableName
        OrmLog.d(sql)
        db.execSQL(sql)
    }

    fun <T : OrmTable> createTable(tableClass: Class<T>) {
        if (Orm.isPrepared()) {
            _createTable(tableClass, Orm.getDB())
        }
    }

    fun <T : OrmTable> upgradeTable(tableClass: Class<T>) {
        if (Orm.isPrepared()) {
            _upgradeTable(tableClass, Orm.getDB())
        }
    }

    fun <T : OrmTable> dropTable(tableClass: Class<T>) {
        if (Orm.isPrepared()) {
            _dropTable(tableClass, Orm.getDB())
        }
    }

    fun <T : OrmTable> recreateTable(tableClass: Class<T>) {
        if (Orm.isPrepared()) {
            Transaction.execute(tableClass) {
                _dropTable(tableClass, db)
                _createTable(tableClass, db)
            }
        }
    }
}