package dora.http

import java.util.concurrent.Executors

class DoraTask(private val task:() -> Unit) {

    private val pool by lazy {
        Executors.newCachedThreadPool()
    }

    fun execute() {
        pool.execute(task)
    }
}