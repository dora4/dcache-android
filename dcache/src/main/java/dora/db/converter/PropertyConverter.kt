package dora.db.converter

interface PropertyConverter<P, D> {

    fun convertToEntityProperty(databaseValue: D?): P?
    fun convertToDatabaseValue(entityProperty: P?): D?
}