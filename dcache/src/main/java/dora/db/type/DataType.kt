package dora.db.type

import dora.db.DataMatcher

enum class DataType /* package */(val matcher: DataMatcher) {

    STRING(StringType.INSTANCE),
    BOOLEAN(BooleanType.INSTANCE),
    CHAR(CharType.INSTANCE),
    BYTE(ByteType.INSTANCE),
    SHORT(ShortType.INSTANCE),
    INT(IntType.INSTANCE),
    LONG(LongType.INSTANCE),
    FLOAT(FloatType.INSTANCE),
    DOUBLE(DoubleType.INSTANCE),
    OTHER(ByteArrayType.INSTANCE);
}