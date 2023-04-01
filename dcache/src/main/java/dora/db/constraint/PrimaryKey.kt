package dora.db.constraint

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class PrimaryKey(val value: AssignType) 