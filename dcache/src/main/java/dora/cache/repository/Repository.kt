package dora.cache.repository

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(RetentionPolicy.RUNTIME)
annotation class Repository(
        /**
         * 缓存策略标记。
         *
         * @see BaseRepository.DataSource.CacheStrategy.NO_CACHE
         *
         * @see BaseRepository.DataSource.CacheStrategy.DATABASE_CACHE
         *
         * @see BaseRepository.DataSource.CacheStrategy.MEMORY_CACHE
         */
        val cacheStrategy: Int = BaseRepository.DataSource.CacheStrategy.NO_CACHE,
        /**
         * 是否是List数据，如果不为List数据，请修改为false。
         *
         * @return
         */
        val isListData: Boolean = true) 