package dora.db.type

/**
 * SQL of the SQLite database is enumerated, and the data types of all other database SQL statements
 * will eventually become the following five.
 * 简体中文：SQLite 数据库的 SQL 中，所有其他数据库 SQL 语句中的数据类型最终都将归类为以下五种。
 */
enum class SqlType {

    /**
     * The value is the null value.
     * 简体中文：该值是空值（NULL 值）。
     */
    NULL,

    /**
     * The value is a signed integer that is stored at 1, 2, 3, 4, 6, or 8 bytes depending on the
     * size of the value.
     * 简体中文：该值是一个带符号的整数，根据值的大小，存储时使用 1、2、3、4、6 或 8 字节。
     */
    INTEGER,

    /**
     * Values are floating-point Numbers, stored in 8-byte IEEE floating point Numbers.
     * 简体中文：值是浮动小数，存储为 8 字节的 IEEE 浮动小数。
     */
    REAL,

    /**
     * The value is a text string and is stored using database encoding
     * (utf-8, utf-16be or utf-16le).
     * 简体中文：该值是一个文本字符串，使用数据库编码（UTF-8、UTF-16BE 或 UTF-16LE）存储。
     */
    TEXT,

    /**
     * A value is a block of data that is stored as it is typed.
     * 简体中文：该值是一块数据，按原样存储。
     */
    BLOB
}