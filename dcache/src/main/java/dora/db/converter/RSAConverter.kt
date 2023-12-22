package dora.db.converter

import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.Charset
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

abstract class RSAConverter : PropertyConverter<String, String> {

    abstract fun getPrivateKey() : String
    abstract fun getPublicKey() : String

    override fun convertToEntityProperty(databaseValue: String?): String? {
        return decryptByPrivate(getPrivateKey(), databaseValue)
    }

    override fun convertToDatabaseValue(entityProperty: String?): String? {
        return entityProperty?.let { encryptByPublic(getPublicKey(), it) }
    }

    /**
     * 使用公钥加密。
     *
     * @param rsa_public
     * @param content
     * @return
     */
    fun encryptByPublic(rsa_public: String, content: String): String? {
        return try {
            val publicKey: RSAPublicKey = getPublicKey(rsa_public)
            val cipher: Cipher = Cipher.getInstance("RSA")
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            Base64.encodeToString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, content.toByteArray(charset("UTF-8")),
                    publicKey.modulus.bitLength()), Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 使用私钥解密。
     *
     * @param rsa_private
     * @param content
     * @return
     */
    fun decryptByPrivate(rsa_private: String, content: String?): String? {
        return try {
            val privateKey: RSAPrivateKey = getPrivateKey(rsa_private)
            val cipher: Cipher = Cipher.getInstance("RSA")
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decode(content, Base64.NO_WRAP), privateKey.modulus.bitLength())!!,
                    Charset.forName("UTF-8"))
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 使用私钥加密。
     *
     * @param rsa_private
     * @param content
     * @return
     */
    fun encryptByPrivate(rsa_private: String, content: String): String? {
        return try {
            val privateKey: RSAPrivateKey = getPrivateKey(rsa_private)
            val cipher: Cipher = Cipher.getInstance("RSA")
            cipher.init(Cipher.ENCRYPT_MODE, privateKey)
            Base64.encodeToString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, content.toByteArray(charset("UTF-8")), privateKey.getModulus().bitLength()),
                    Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 使用公钥解密。
     *
     * @param rsa_public
     * @param content
     * @return
     */
    fun decryptByPublic(rsa_public: String, content: String?): String? {
        return try {
            val publicKey: RSAPublicKey = getPublicKey(rsa_public)
            val cipher: Cipher = Cipher.getInstance("RSA")
            cipher.init(Cipher.DECRYPT_MODE, publicKey)
            String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decode(content, Base64.NO_WRAP),
                    publicKey.modulus.bitLength())!!, Charset.forName("UTF-8"))
        } catch (e: Exception) {
            ""
        }
    }

    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    fun getPublicKey(publicKey: String?): RSAPublicKey {
        //通过X509编码的Key指令获得公钥对象
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        val x509KeySpec = X509EncodedKeySpec(Base64.decode(publicKey, Base64.NO_WRAP))
        return keyFactory.generatePublic(x509KeySpec) as RSAPublicKey
    }

    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    fun getPrivateKey(privateKey: String?): RSAPrivateKey {
        //通过PKCS#8编码的Key指令获得私钥对象
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        val pkcs8KeySpec = PKCS8EncodedKeySpec(Base64.decode(privateKey, Base64.NO_WRAP))
        return keyFactory.generatePrivate(pkcs8KeySpec) as RSAPrivateKey
    }

    private fun rsaSplitCodec(cipher: Cipher, opmode: Int, datas: ByteArray, keySize: Int): ByteArray? {
        val maxBlock: Int = if (opmode == Cipher.DECRYPT_MODE) {
            keySize / 8
        } else {
            keySize / 8 - 11
        }
        val out = ByteArrayOutputStream()
        var offSet = 0
        var buff: ByteArray
        var i = 0
        try {
            while (datas.size > offSet) {
                buff = if (datas.size - offSet > maxBlock) {
                    cipher.doFinal(datas, offSet, maxBlock)
                } else {
                    cipher.doFinal(datas, offSet, datas.size - offSet)
                }
                out.write(buff, 0, buff.size)
                i++
                offSet = i * maxBlock
            }
        } catch (e: Exception) {
            throw RuntimeException("加解密阀值为[$maxBlock]的数据时发生异常", e)
        }
        val result: ByteArray = out.toByteArray()
        try {
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }
}