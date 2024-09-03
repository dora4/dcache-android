package dora.http.log

import java.io.*
import java.lang.UnsupportedOperationException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.zip.*

class ZipHelper private constructor() {

    companion object {

        @JvmOverloads
        fun decompressToStringForZlib(bytesToDecompress: ByteArray, charsetName: String? = "UTF-8"): String? {
            val bytesDecompressed = decompressForZlib(
                    bytesToDecompress
            )
            var returnValue: String? = null
            try {
                returnValue = String(
                        bytesDecompressed!!,
                        0,
                        bytesDecompressed.size,
                        Charset.forName(charsetName)
                )
            } catch (uee: UnsupportedEncodingException) {
                uee.printStackTrace()
            }
            return returnValue
        }

        fun decompressForZlib(bytesToDecompress: ByteArray): ByteArray? {
            var returnValues: ByteArray? = null
            val inflater = Inflater()
            val numberOfBytesToDecompress = bytesToDecompress.size
            inflater.setInput(
                    bytesToDecompress,
                    0,
                    numberOfBytesToDecompress
            )
            var numberOfBytesDecompressedSoFar = 0
            val bytesDecompressedSoFar: MutableList<Byte> = ArrayList()
            try {
                while (!inflater.needsInput()) {
                    val bytesDecompressedBuffer = ByteArray(numberOfBytesToDecompress)
                    val numberOfBytesDecompressedThisTime = inflater.inflate(
                            bytesDecompressedBuffer
                    )
                    numberOfBytesDecompressedSoFar += numberOfBytesDecompressedThisTime
                    for (b in 0 until numberOfBytesDecompressedThisTime) {
                        bytesDecompressedSoFar.add(bytesDecompressedBuffer[b])
                    }
                }
                returnValues = ByteArray(bytesDecompressedSoFar.size)
                for (b in returnValues.indices) {
                    returnValues[b] = bytesDecompressedSoFar[b]
                }
            } catch (dfe: DataFormatException) {
                dfe.printStackTrace()
            }
            inflater.end()
            return returnValues
        }

        fun compressForZlib(bytesToCompress: ByteArray?): ByteArray {
            val deflater = Deflater()
            deflater.setInput(bytesToCompress)
            deflater.finish()
            val bytesCompressed = ByteArray(Short.MAX_VALUE.toInt())
            val numberOfBytesAfterCompression = deflater.deflate(bytesCompressed)
            val returnValues = ByteArray(numberOfBytesAfterCompression)
            System.arraycopy(
                    bytesCompressed,
                    0,
                    returnValues,
                    0,
                    numberOfBytesAfterCompression
            )
            return returnValues
        }

        fun compressForZlib(stringToCompress: String): ByteArray? {
            var returnValues: ByteArray? = null
            try {
                returnValues =
                    compressForZlib(
                            stringToCompress.toByteArray(StandardCharsets.UTF_8)
                    )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return returnValues
        }

        fun compressForGzip(string: String): ByteArray? {
            var os: ByteArrayOutputStream? = null
            var gos: GZIPOutputStream? = null
            try {
                os = ByteArrayOutputStream(string.length)
                gos = GZIPOutputStream(os)
                gos.write(string.toByteArray(StandardCharsets.UTF_8))
                return os.toByteArray()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                closeQuietly(gos)
                closeQuietly(os)
            }
            return null
        }

        @JvmOverloads
        fun decompressForGzip(compressed: ByteArray, charsetName: String? = "UTF-8"): String? {
            val BUFFER_SIZE = compressed.size
            var gis: GZIPInputStream? = null
            var `is`: ByteArrayInputStream? = null
            try {
                `is` = ByteArrayInputStream(compressed)
                gis = GZIPInputStream(`is`, BUFFER_SIZE)
                val string = StringBuilder()
                val data = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                while (gis.read(data).also { bytesRead = it } != -1) {
                    string.append(String(data, 0, bytesRead, Charset.forName(charsetName)))
                }
                return string.toString()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                closeQuietly(gis)
                closeQuietly(`is`)
            }
            return null
        }

        private fun closeQuietly(closeable: Closeable?) {
            if (closeable != null) {
                try {
                    closeable.close()
                } catch (rethrown: RuntimeException) {
                    throw rethrown
                } catch (ignored: Exception) {
                }
            }
        }
    }

    init {
        throw UnsupportedOperationException("you can't instantiate me!")
    }
}