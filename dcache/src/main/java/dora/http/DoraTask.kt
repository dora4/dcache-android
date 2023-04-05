package dora.http

import java.util.concurrent.Executors

internal class DoraTask(private val task:() -> Unit) {

    private val pool by lazy {
        Executors.newCachedThreadPool()
    }

    fun execute() {
        return pool.execute(task)
    }
}