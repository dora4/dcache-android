package dora.db.converter

import android.text.TextUtils
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

abstract class DesConverter : PropertyConverter<String, String> {

    abstract fun getDesKey() : ByteArray

    override fun convertToEntityProperty(databaseValue: String?): String? {
        return decryptDES(getDesKey(), databaseValue)
    }

    override fun convertToDatabaseValue(entityProperty: String?): String? {
        return entityProperty?.let { encryptDES(getDesKey(), it) }
    }

    @Throws(Exception::class)
    open fun encryptDES(key: ByteArray, encryptString: String): String? {
        val zeroIv = IvParameterSpec(ByteArray(key.size))
        val secretKeySpec = SecretKeySpec(key, "DES")
        val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, zeroIv)
        val encryptedData = cipher.doFinal(encryptString.toByteArray())
        return bytes2hex(encryptedData)
    }

    @Throws(Exception::class)
    open fun decryptDES(key: ByteArray, decryptString: String?): String? {
        if (decryptString != null && !TextUtils.isEmpty(decryptString)) {
            val byteMi: ByteArray = hex2bytes(decryptString, "")
            val zeroIv = IvParameterSpec(ByteArray(key.size))
            val secretKeySpec = SecretKeySpec(key, "DES")
            val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, zeroIv)
            val decryptedData = cipher.doFinal(byteMi)
            return String(decryptedData)
        }
        return ""
    }

    open fun bytes2hex(src: ByteArray?, separator: String): String? {
        if (src == null) {
            return null
        }
        val buffer = StringBuilder()
        for (i in src.indices) {
            val value: Int = (src[i] and 0xFF.toByte()).toInt()
            val H: String = Integer.toHexString(value)
            if (H.length < 2) {
                buffer.append(0)
            }
            buffer.append(H).append(separator)
        }
        return buffer.substring(0, buffer.length - separator.length)
    }

    private fun bytes2hex(src: ByteArray?): String? {
        return bytes2hex(src, "")
    }

    private fun hex2bytes(hexString: String, separator: String): ByteArray {
        val hexArr = hexString.split(separator.toRegex()).toTypedArray()
        val bs = ByteArray(hexArr.size)
        var i = 0
        for (b in hexArr) {
            bs[i++] = Integer.valueOf(b, 16).toByte()
        }
        return bs
    }
}