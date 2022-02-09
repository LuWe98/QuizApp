package com.example.quizapp.utils

import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object EncryptionUtil {

    private val secretKey = Secrets.datastoreEncryptionSecretKey()
    private val salt = Secrets.datastoreEncryptionSalt()
    private val iv = Secrets.datastoreEncryptionIv()

    private const val TRANSFORMATION = "AES/CBC/PKCS7Padding"
    private const val ALGORITHM = "PBKDF2WithHmacSHA1"
    private const val ITERATION_COUNT = 1_000
    private const val KEY_LENGTH = 256


    fun String.decrypt() = decryptInternal(this)

    fun String.encrypt(): String = encryptInternal(this)

    inline fun <reified T> T.encryptObject(): String = Json.encodeToString(this).decrypt()

    inline fun <reified T> String.decryptToObject(): T = Json.decodeFromString(encrypt())


    private fun encryptInternal(strToEncrypt: String) = Base64.encodeToString(
        useCipher(strToEncrypt, Cipher.ENCRYPT_MODE),
        Base64.DEFAULT
    )

    private fun decryptInternal(strToDecrypt: String) = useCipher(
        Base64.decode(strToDecrypt, Base64.DEFAULT),
        Cipher.DECRYPT_MODE
    ).decodeToString()


    private fun useCipher(input: String, cipherMode: Int) = useCipher(input.toByteArray(Charsets.UTF_8), cipherMode)

    private fun useCipher(input: ByteArray, cipherMode: Int): ByteArray {
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val spec = PBEKeySpec(secretKey.toCharArray(), Base64.decode(salt, Base64.DEFAULT), ITERATION_COUNT, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        val secretKey = SecretKeySpec(tmp.encoded, KeyProperties.KEY_ALGORITHM_AES)
        val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

        return Cipher.getInstance(TRANSFORMATION).apply {
            init(cipherMode, secretKey, ivParameterSpec)
        }.doFinal(input)
    }



//    private fun initCipher(mode: Int): Cipher {
//        val factory = SecretKeyFactory.getInstance(ALGORITHM)
//        val spec = PBEKeySpec(secretKey.toCharArray(), Base64.decode(salt, Base64.DEFAULT), ITERATION_COUNT, KEY_LENGTH)
//        val tmp = factory.generateSecret(spec)
//        val secretKey = SecretKeySpec(tmp.encoded, KeyProperties.KEY_ALGORITHM_AES)
//        val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))
//
//        return Cipher.getInstance(TRANSFORMATION).apply {
//            init(mode, secretKey, ivParameterSpec)
//        }
//    }
//    private fun encryptInternal(strToEncrypt: String): String {
//        val cipher = initCipher(Cipher.ENCRYPT_MODE)
//        return Base64.encodeToString(cipher.doFinal(strToEncrypt.toByteArray(Charsets.UTF_8)), Base64.DEFAULT)
//    }
//    private fun decryptInternal(strToDecrypt: String): String {
//        val cipher = initCipher(Cipher.DECRYPT_MODE)
//        return cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT)).decodeToString()
//    }
}