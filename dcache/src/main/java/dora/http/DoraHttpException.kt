package dora.http

import okhttp3.Request
import java.lang.RuntimeException

class DoraHttpException(request: Request, msg: String?)
    : RuntimeException("send ${request.url} return $msg")