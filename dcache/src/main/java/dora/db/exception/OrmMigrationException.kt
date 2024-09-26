package dora.db.exception

/**
 * Database migration exception; please check that the new version number in
 * [dora.db.migration.OrmMigration] is not less than the old version number.
 * 简体中文：数据库迁移异常，请检查[dora.db.migration.OrmMigration]中新版本号是否不小于旧版本号。
 */
class OrmMigrationException(message: String) : IllegalStateException(message)