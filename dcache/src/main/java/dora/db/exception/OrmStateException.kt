package dora.db.exception

class OrmStateException : IllegalStateException {
    constructor(message: String) : super(message)
}