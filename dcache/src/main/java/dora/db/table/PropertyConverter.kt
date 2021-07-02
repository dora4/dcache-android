package dora.db.table

interface PropertyConverter<P, D> {
    fun convertToEntityProperty(databaseValue: D?): P?
    fun convertToDatabaseValue(entityProperty: P?): D?
}