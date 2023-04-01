package dora.http.exception

import okhttp3.Request
import java.lang.RuntimeException

class DoraHttpException(message: String) : RuntimeException(message) {

    constructor(request: Request, msg: String?) : this("send ${request.url} return $msg")
}