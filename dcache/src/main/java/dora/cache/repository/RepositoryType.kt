package dora.cache.repository

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(RetentionPolicy.RUNTIME)
annotation class RepositoryType(

    @BaseRepository.Strategy
    val cacheStrategy: Int = BaseRepository.CacheStrategy.NO_CACHE
)
