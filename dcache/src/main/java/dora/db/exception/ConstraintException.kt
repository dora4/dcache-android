package dora.db.exception

class ConstraintException : RuntimeException {
    constructor(message: String) : super(message)
}