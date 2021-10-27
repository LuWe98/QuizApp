package com.example.quizapp.utils

import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

//TODO -> Hashing mit Salt macht DB immun gegen rainbow table attacks
object EncryptionUtil {

    private val secretKey = Secrets.datastoreEncryptionSecretKey()
    private val salt = Secrets.datastoreEncryptionSalt()
    private val iv = Secrets.datastoreEncryptionIv()

    private const val TRANSFORMATION = "AES/CBC/PKCS7Padding"
    private const val ALGORITHM = "PBKDF2WithHmacSHA1"
    private const val ITERATION_COUNT = 2500
    private const val KEY_LENGTH = 256


    fun String.decrypt() = decryptInternal(this)

    fun String.encrypt() = encryptInternal(this)


    @ExperimentalSerializationApi
    inline fun <reified T> encryptObject(value: T) = Json.encodeToString(value).decrypt()

    @ExperimentalSerializationApi
    inline fun <reified T> decryptToObject(strToDecrypt: String) = Json.decodeFromString<T>(strToDecrypt.encrypt())



    private fun encryptInternal(strToEncrypt: String): String {
        val cipher = initCipher(Cipher.ENCRYPT_MODE)
        return Base64.encodeToString(cipher.doFinal(strToEncrypt.toByteArray(Charsets.UTF_8)), Base64.DEFAULT)
    }

    private fun decryptInternal(strToDecrypt: String): String {
        val cipher = initCipher(Cipher.DECRYPT_MODE)
        return String(cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT)))
    }


    private fun initCipher(mode: Int): Cipher {
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val spec = PBEKeySpec(secretKey.toCharArray(), Base64.decode(salt, Base64.DEFAULT), ITERATION_COUNT, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        val secretKey = SecretKeySpec(tmp.encoded, KeyProperties.KEY_ALGORITHM_AES)
        val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

        return Cipher.getInstance(TRANSFORMATION).apply {
            init(mode, secretKey, ivParameterSpec)
        }
    }
}

//    val secretKey = "tK5UTui+DPh8lIlBxya5XVsmeDCoUl6vHhdIESMB6sQ="
//    val salt = "QWlGNHNhMTJTQWZ2bGhpV3U=" // base64 decode => AiF4sa12SAfvlhiWu
//    val iv = "bVQzNFNhRkQ1Njc4UUFaWA==" // base64 decode => mT34SaFD5678QAZX