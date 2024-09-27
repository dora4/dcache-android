package dora.http.log

import android.text.TextUtils
import android.util.Log
import dora.http.log.CharacterHandler.Companion.jsonFormat
import dora.http.log.CharacterHandler.Companion.xmlFormat
import okhttp3.MediaType
import okhttp3.Request

class DefaultFormatPrinter : FormatPrinter {

    override fun printJsonRequest(request: Request, bodyString: String) {
        val requestBody = LINE_SEPARATOR + BODY_TAG + LINE_SEPARATOR + bodyString
        val tag = getTag(true)
        Log.d(tag, REQUEST_UP_LINE)
        logLines(tag, arrayOf(URL_TAG + request.url), false)
        logLines(tag, getRequest(request), true)
        logLines(tag, requestBody.split(LINE_SEPARATOR).toTypedArray(), true)
        Log.d(tag, END_LINE)
    }

    override fun printFileRequest(request: Request) {
        val tag = getTag(true)
        Log.d(tag, REQUEST_UP_LINE)
        logLines(tag, arrayOf(URL_TAG + request.url), false)
        logLines(tag, getRequest(request), true)
        logLines(tag, OMITTED_REQUEST, true)
        Log.d(tag, END_LINE)
    }

    override fun printJsonResponse(chainMs: Long, isSuccessful: Boolean, code: Int, headers: String, contentType: MediaType?,
                                   bodyString: String?, segments: List<String>, message: String, responseUrl: String) {
        var bodyString = bodyString
        bodyString = if (FormatLogInterceptor.isJson(contentType)) jsonFormat(bodyString!!) else if (FormatLogInterceptor.isXml(contentType)) xmlFormat(bodyString) else bodyString
        val responseBody = LINE_SEPARATOR + BODY_TAG + LINE_SEPARATOR + bodyString
        val tag = getTag(false)
        val urlLine = arrayOf(URL_TAG + responseUrl, N)
        Log.d(tag, RESPONSE_UP_LINE)
        logLines(tag, urlLine, true)
        logLines(tag, getResponse(headers, chainMs, code, isSuccessful, segments, message), true)
        logLines(tag, responseBody.split(LINE_SEPARATOR).toTypedArray(), true)
        Log.d(tag, END_LINE)
    }

    override fun printFileResponse(chainMs: Long, isSuccessful: Boolean, code: Int, headers: String,
                                   segments: List<String>, message: String, responseUrl: String) {
        val tag = getTag(false)
        val urlLine = arrayOf(URL_TAG + responseUrl, N)
        Log.d(tag, RESPONSE_UP_LINE)
        logLines(tag, urlLine, true)
        logLines(tag, getResponse(headers, chainMs, code, isSuccessful, segments, message), true)
        logLines(tag, OMITTED_RESPONSE, true)
        Log.d(tag, END_LINE)
    }

    companion object {
        private const val TAG = "DoraHttpLog"
        private val LINE_SEPARATOR = System.getProperty("line.separator")
        private val DOUBLE_SEPARATOR = LINE_SEPARATOR?.plus(LINE_SEPARATOR)
        private val OMITTED_RESPONSE = arrayOf(LINE_SEPARATOR, "Omitted response body")
        private val OMITTED_REQUEST = arrayOf(LINE_SEPARATOR, "Omitted request body")
        private const val N = "\n"
        private const val T = "\t"
        private const val REQUEST_UP_LINE = " ┌────── Request ────────────────────────────────────────────────────────────────────────"
        private const val END_LINE = " └───────────────────────────────────────────────────────────────────────────────────────"
        private const val RESPONSE_UP_LINE = " ┌────── Response ───────────────────────────────────────────────────────────────────────"
        private const val BODY_TAG = "Body:"
        private const val URL_TAG = "URL: "
        private const val METHOD_TAG = "Method: @"
        private const val HEADERS_TAG = "Headers:"
        private const val STATUS_CODE_TAG = "Status Code: "
        private const val RECEIVED_TAG = "Received in: "
        private const val CORNER_UP = " ┌ "
        private const val CORNER_BOTTOM = " └ "
        private const val CENTER_LINE = " ├ "
        private const val DEFAULT_LINE = " │ "
        private val DORA = arrayOf("-D-", "-O-", "-R-", "-A-")
        private val last: ThreadLocal<Int> = object : ThreadLocal<Int>() {
            override fun initialValue(): Int {
                return 0
            }
        }

        private fun isEmpty(line: String): Boolean {
            return TextUtils.isEmpty(line) || N == line || T == line || TextUtils.isEmpty(line.trim { it <= ' ' })
        }

        private fun logLines(tag: String, lines: Array<String>, withLineSize: Boolean) {
            for (line in lines) {
                val lineLength = line.length
                val maxLongSize = if (withLineSize) 110 else lineLength
                for (i in 0..lineLength / maxLongSize) {
                    val start = i * maxLongSize
                    var end = (i + 1) * maxLongSize
                    end = if (end > line.length) line.length else end
                    Log.d(resolveTag(tag), DEFAULT_LINE + line.substring(start, end))
                }
            }
        }

        private fun computeKey(): String {
            if (last.get() >= 4) {
                last.set(0)
            }
            val s = DORA[last.get()]
            last.set(last.get() + 1)
            return s
        }

        /**
         * This method is designed to address the issue of misaligned log outputs in Logcat for
         * Android Studio versions 3.1 and above. The problem arises because Android Studio 3.1 and
         * later automatically merges multiple logs with the same tag that are output in a very
         * short time frame, causing originally symmetrical log outputs to appear asymmetrical.
         * This optimization in Android Studio 3.1 inadvertently caused all logging frameworks with
         * formatting output capabilities to malfunction.
         * Currently, two temporary solutions can be considered:
         * 1. Change the tag of each line (by adding a variable token to each line's tag).
         * 2. Introduce a delay between each log output.
         * The `[resolveTag]` method uses the first solution.
         * 简体中文：此方法是为了解决在 AndroidStudio v3.1 以上 Logcat 输出的日志无法对齐的问题。
         * 此问题引起的原因，可能是因为 AndroidStudio v3.1 以上将极短时间内以相同 tag 输出多次的 log 自动合并为
         * 一次输出导致本来对称的输出日志，出现不对称的问题。
         * AndroidStudio v3.1 此次对输出日志的优化，不小心使市面上所有具有日志格式化输出功能的日志框架无法正常工作
         * 现在暂时能想到的解决方案有两个: 1. 改变每行的 tag (每行 tag 都加一个可变化的 token) 2. 延迟每行日志打
         * 印的间隔时间，[resolveTag] 使用第一种解决方案。
         *
         * @param tag
         */
        private fun resolveTag(tag: String): String {
            return computeKey() + tag
        }

        private fun getRequest(request: Request): Array<String> {
            val log: String
            val header = request.headers.toString()
            log = METHOD_TAG + request.method + DOUBLE_SEPARATOR +
                    if (isEmpty(header)) "" else HEADERS_TAG + LINE_SEPARATOR + dotHeaders(header)
            return log.split(LINE_SEPARATOR).toTypedArray()
        }

        private fun getResponse(header: String, tookMs: Long, code: Int, isSuccessful: Boolean,
                                segments: List<String>, message: String): Array<String> {
            val log: String
            val segmentString = slashSegments(segments)
            log = ((if (!TextUtils.isEmpty(segmentString)) "$segmentString - " else "") + "is success : "
                    + isSuccessful + " - " + RECEIVED_TAG + tookMs + "ms" + DOUBLE_SEPARATOR + STATUS_CODE_TAG +
                    code + " / " + message + DOUBLE_SEPARATOR + if (isEmpty(header)) "" else HEADERS_TAG + LINE_SEPARATOR +
                    dotHeaders(header))
            return log.split(LINE_SEPARATOR).toTypedArray()
        }

        private fun slashSegments(segments: List<String>): String {
            val segmentString = StringBuilder()
            for (segment in segments) {
                segmentString.append("/").append(segment)
            }
            return segmentString.toString()
        }

        /**
         * 对 `header` 按规定的格式进行处理。
         *
         * @param header
         * @return
         */
        private fun dotHeaders(header: String): String {
            val headers = header.split(LINE_SEPARATOR).toTypedArray()
            val builder = StringBuilder()
            var tag = "─ "
            if (headers.size > 1) {
                for (i in headers.indices) {
                    tag = if (i == 0) {
                        CORNER_UP
                    } else if (i == headers.size - 1) {
                        CORNER_BOTTOM
                    } else {
                        CENTER_LINE
                    }
                    builder.append(tag).append(headers[i]).append("\n")
                }
            } else {
                for (item in headers) {
                    builder.append(tag).append(item).append("\n")
                }
            }
            return builder.toString()
        }

        private fun getTag(isRequest: Boolean): String {
            return if (isRequest) {
                "$TAG-Request"
            } else {
                "$TAG-Response"
            }
        }
    }
}