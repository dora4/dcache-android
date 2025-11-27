package dora.http.exception

import okhttp3.Request
import java.lang.RuntimeException

/**
 * Custom HTTP exception class used to wrap error information from network requests.
 * 简体中文：自定义 HTTP 异常类，用于封装网络请求中的错误信息。
 *
 * @param message The description of the exception. 简体中文：异常描述信息
 */
class DoraHttpException(message: String) : RuntimeException(message) {

    constructor(request: Request, msg: String?) : this("send ${request.url} return $msg")
}