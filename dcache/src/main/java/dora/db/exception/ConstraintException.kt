package dora.db.exception

/**
 * 约束异常。
 */
class ConstraintException : RuntimeException {
    constructor(message: String) : super(message)
}