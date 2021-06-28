package dora.http

class ApiResult<T> {
    var code: Int? = null
    var msg: String? = null
    var data: T? = null
        private set
    val timestamp = System.currentTimeMillis()

    fun setData(data: T) {
        this.data = data
    }
}