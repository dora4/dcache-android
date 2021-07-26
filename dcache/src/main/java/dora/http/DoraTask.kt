package dora.http

import java.util.concurrent.Executors

class DoraTask<T>(private val task:() -> T) {

    private val pool by lazy {
        Executors.newCachedThreadPool()
    }

    fun execute() : T {
        return pool.submit(task).get()
    }
}