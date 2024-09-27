package dora.http.log

import android.util.Log
import dora.http.log.CharacterHandler.Companion.jsonFormat
import okhttp3.*
import okio.Buffer
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class FormatLogInterceptor : Interceptor {

    var printer: FormatPrinter = DefaultFormatPrinter()
    var printLevel = Level.ALL

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val logRequest = printLevel == Level.ALL || printLevel != Level.NONE && printLevel == Level.REQUEST
        if (logRequest) {
            if (request.body != null && isParseable(request.body!!.contentType())) {
                printer.printJsonRequest(request, parseParams(request))
            } else {
                printer.printFileRequest(request)
            }
        }
        val logResponse = printLevel == Level.ALL || printLevel != Level.NONE && printLevel == Level.RESPONSE
        val t1 = if (logResponse) System.nanoTime() else 0
        val originalResponse: Response
        originalResponse = try {
            chain.proceed(request)
        } catch (e: Exception) {
            Log.w("Http Error: %s", e)
            throw e
        }
        val t2 = if (logResponse) System.nanoTime() else 0
        val responseBody = originalResponse.body

        var bodyString: String? = null
        if (responseBody != null && isParseable(responseBody.contentType())) {
            bodyString = printResult(request, originalResponse, logResponse)
        }
        if (logResponse) {
            val segmentList: List<String> = request.url.encodedPathSegments
            val header = originalResponse.headers.toString()
            val code = originalResponse.code
            val isSuccessful = originalResponse.isSuccessful
            val message = originalResponse.message
            val url = originalResponse.request.url.toString()
            if (responseBody != null && isParseable(responseBody.contentType())) {
                printer.printJsonResponse(TimeUnit.NANOSECONDS.toMillis(t2 - t1), isSuccessful,
                        code, header, responseBody.contentType(), bodyString, segmentList, message, url)
            } else {
                printer.printFileResponse(TimeUnit.NANOSECONDS.toMillis(t2 - t1),
                        isSuccessful, code, header, segmentList, message, url)
            }
        }
        return originalResponse
    }

    @Throws(IOException::class)
    private fun printResult(request: Request, response: Response, logResponse: Boolean): String? {
        return try {
            val responseBody = response.newBuilder().build().body
            val source = responseBody!!.source()
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer()

            val encoding = response
                    .headers["Content-Encoding"]
            val clone = buffer.clone()

            parseContent(responseBody, encoding, clone)
        } catch (e: IOException) {
            e.printStackTrace()
            "{\"error\": \"" + e.message + "\"}"
        }
    }

    private fun parseContent(responseBody: ResponseBody?, encoding: String?, clone: Buffer): String? {
        var charset = Charset.forName("UTF-8")
        val contentType = responseBody!!.contentType()
        if (contentType != null) {
            charset = contentType.charset(charset)
        }
        return if ("gzip".equals(encoding, ignoreCase = true)) {
            ZipHelper.decompressForGzip(clone.readByteArray(), convertCharset(charset))
        } else if ("zlib".equals(encoding, ignoreCase = true)) {
            ZipHelper.decompressToStringForZlib(clone.readByteArray(), convertCharset(charset))
        } else {
            clone.readString(charset!!)
        }
    }

    enum class Level {

        /**
         * Do not print logs.
         * 简体中文：不打印log。
         */
        NONE,

        /**
         * Only print request information.
         * 简体中文：只打印请求信息。
         */
        REQUEST,

        /**
         * Only print response information.
         * 简体中文：只打印响应信息。
         */
        RESPONSE,

        /**
         * Print all data.
         * 简体中文：所有数据全部打印。
         */
        ALL
    }

    companion object {

        @Throws(UnsupportedEncodingException::class)
        fun parseParams(request: Request): String {
            return try {
                val body = request.newBuilder().build().body ?: return ""
                val requestbuffer = Buffer()
                body.writeTo(requestbuffer)
                var charset = Charset.forName("UTF-8")
                val contentType = body.contentType()
                if (contentType != null) {
                    charset = contentType.charset(charset)
                }
                var json: String = requestbuffer.readString(charset!!)
                if (UrlEncoderUtils.hasUrlEncoded(json)) {
                    json = URLDecoder.decode(json, convertCharset(charset))
                }
                jsonFormat(json!!)
            } catch (e: IOException) {
                e.printStackTrace()
                "{\"error\": \"" + e.message + "\"}"
            }
        }

        fun isParseable(mediaType: MediaType?): Boolean {
            return if (mediaType == null || mediaType.type == null) {
                false
            } else isText(mediaType) || isPlain(mediaType)
                    || isJson(mediaType) || isForm(mediaType)
                    || isHtml(mediaType) || isXml(mediaType)
        }

        fun isText(mediaType: MediaType?): Boolean {
            return if (mediaType == null || mediaType.type == null) {
                false
            } else "text" == mediaType.type
        }

        fun isPlain(mediaType: MediaType?): Boolean {
            return if (mediaType == null || mediaType.subtype == null) {
                false
            } else mediaType.subtype.toLowerCase().contains("plain")
        }

        fun isJson(mediaType: MediaType?): Boolean {
            return if (mediaType == null || mediaType.subtype == null) {
                false
            } else mediaType.subtype.toLowerCase().contains("json")
        }

        fun isXml(mediaType: MediaType?): Boolean {
            return if (mediaType == null || mediaType.subtype == null) {
                false
            } else mediaType.subtype.toLowerCase().contains("xml")
        }

        fun isHtml(mediaType: MediaType?): Boolean {
            return if (mediaType == null || mediaType.subtype == null) {
                false
            } else mediaType.subtype.toLowerCase().contains("html")
        }

        fun isForm(mediaType: MediaType?): Boolean {
            return if (mediaType == null || mediaType.subtype == null) {
                false
            } else mediaType.subtype.toLowerCase().contains("x-www-form-urlencoded")
        }

        fun convertCharset(charset: Charset?): String {
            val s = charset.toString()
            val i = s.indexOf("[")
            return if (i == -1) {
                s
            } else s.substring(i + 1, s.length - 1)
        }
    }
}