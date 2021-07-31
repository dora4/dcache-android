package dora.cache.repository

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.reflect.KClass

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(RetentionPolicy.RUNTIME)
annotation class Repository(

        /**
         * 是否是List数据，如果不为List数据，请修改为false。
         *
         * @return
         */
        val isListMode: Boolean = true)