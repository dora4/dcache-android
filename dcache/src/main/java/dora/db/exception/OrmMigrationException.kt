package dora.db.exception

class OrmMigrationException : IllegalStateException {
    constructor(message: String) : super(message)
}