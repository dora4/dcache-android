package dora.http.log

class UrlEncoderUtils private constructor() {

    companion object {

        /**
         * Determine whether `str` has already been encoded with `URLEncoder.encode()`.
         * It is common to encounter situations where you have a URL, but you are unsure whether
         * you need to apply `URLEncoder.encode()`. If you don't apply `URLEncoder.encode()`, you
         * worry about potential errors, but if you do apply it, you are concerned about duplicating
         * the encoding.
         * 简体中文：判断 str 是否已经 URLEncoder.encode() 过
         * 经常遇到这样的情况, 拿到一个 URL，但是搞不清楚到底要不要 URLEncoder.encode()
         * 不做 URLEncoder.encode() 吧，担心出错，做 URLEncoder.encode() 吧，又怕重复了。
         *
         * @param str The content that needs to be checked. 简体中文：需要判断的内容
         * @return Return `true` if it has been encoded with `URLEncoder.encode()`. 简体中文：返回
         * `true` 为被 URLEncoder.encode() 过
         */
        fun hasUrlEncoded(str: String): Boolean {
            var encode = false
            for (i in str.indices) {
                val c = str[i]
                if (c == '%' && i + 2 < str.length) {
                    val c1 = str[i + 1]
                    val c2 = str[i + 2]
                    if (isValidHexChar(c1) && isValidHexChar(c2)) {
                        encode = true
                        break
                    }
                }
            }
            return encode
        }

        /**
         * Check if `c` is a hexadecimal character.
         * 简体中文：判断 c 是否是 16 进制的字符。
         *
         * @param c The character to be checked. 简体中文：需要判断的字符
         * @return Return `true` if it is a hexadecimal character. 简体中文：返回 `true` 为 16 进制的字符
         */
        private fun isValidHexChar(c: Char): Boolean {
            return c in '0'..'9' || c in 'a'..'f' || c in 'A'..'F'
        }
    }

    init {
        throw IllegalStateException("you can't instantiate me!")
    }
}