package dora.cache.repository

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * 每一个具体的Repository类必须配置这个注解，来标记是否为List类型的数据。
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(RetentionPolicy.RUNTIME)
annotation class Repository(

        /**
         * 是否是List类型的数据，如果不为List类型的数据，请修改为false。
         *
         * @return
         */
        val isListMode: Boolean = true,

        /**
         * 是否打印调试日志。
         */
        val isLogPrint: Boolean = false)