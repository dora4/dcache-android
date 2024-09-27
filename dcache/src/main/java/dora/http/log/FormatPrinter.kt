package dora.http.log

import okhttp3.MediaType
import okhttp3.Request

interface FormatPrinter {

    /**
     * Print network request information when the network request is in a parsable state, such as
     * when `{[okhttp3.RequestBody]}` can be parsed.
     * 简体中文：打印网络请求信息，当网络请求时 {[okhttp3.RequestBody]} 可以解析的情况。
     *
     * @param request
     * @param bodyString The data in the request body sent to the server (parsed). 简体中文：发送给服务
     * 器的请求体中的数据(已解析)
     */
    fun printJsonRequest(request: Request, bodyString: String)

    /**
     * Print network request information when the network request is in a non-parsable state, such
     * as when `{[okhttp3.RequestBody]}` is `null` or cannot be parsed.
     * 简体中文：打印网络请求信息，当网络请求时 {[okhttp3.RequestBody]} 为 `null` 或不可解析的情况。
     *
     * @param request
     */
    fun printFileRequest(request: Request)

    /**
     * Print network response information when the network response is in a parsable state, such as
     * when it's `{[okhttp3.ResponseBody]}`.
     * 简体中文：打印网络响应信息，当网络响应时 {[okhttp3.ResponseBody]} 可以解析的情况。
     *
     * @param chainMs      Server response time (in milliseconds). 简体中文：服务器响应耗时(单位毫秒)
     * @param isSuccessful Whether the request was successful. 简体中文：请求是否成功
     * @param code         Response code. 简体中文：响应码
     * @param headers      Request headers. 简体中文：请求头
     * @param contentType  The data type of the data returned by the server. 简体中文：服务器返回数据的数据类型
     * @param bodyString   The data returned by the server (parsed). 简体中文：服务器返回的数据(已解析)
     * @param segments     The resource address following the domain name. 简体中文：域名后面的资源地址
     * @param message      Response information. 简体中文：响应信息
     * @param responseUrl  Request address. 简体中文：请求地址
     */
    fun printJsonResponse(chainMs: Long, isSuccessful: Boolean, code: Int, headers: String, contentType: MediaType?,
                          bodyString: String?, segments: List<String>, message: String, responseUrl: String)

    /**
     * Print network response information when the network response is `{[okhttp3.ResponseBody]}`
     * is `null` or in a non-parsable state.
     * 简体中文：打印网络响应信息，当网络响应时 {[okhttp3.ResponseBody]} 为 `null` 或不可解析的情况。
     *
     * @param chainMs      Server response time (in milliseconds). 简体中文：服务器响应耗时(单位毫秒)
     * @param isSuccessful Whether the request was successful. 简体中文：请求是否成功
     * @param code         Response code. 简体中文：响应码
     * @param headers      Request headers. 简体中文：请求头
     * @param segments     The resource address following the domain name. 简体中文：域名后面的资源地址
     * @param message      Response information. 简体中文：响应信息
     * @param responseUrl  Request address. 简体中文：请求地址
     */
    fun printFileResponse(chainMs: Long, isSuccessful: Boolean, code: Int, headers: String,
                          segments: List<String>, message: String, responseUrl: String)
}