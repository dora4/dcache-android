package dora.http.log

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlEncoderUtilsTest {

    @Test
    fun hasUrlEncoded_detectsValidSequences() {
        assertTrue(UrlEncoderUtils.hasUrlEncoded("abc%20def"))
    }

    @Test
    fun hasUrlEncoded_ignoresInvalidSequences() {
        assertTrue(UrlEncoderUtils.hasUrlEncoded("abc%2G%20def"))
        assertFalse(UrlEncoderUtils.hasUrlEncoded("abc%2Gdef"))
    }
}
