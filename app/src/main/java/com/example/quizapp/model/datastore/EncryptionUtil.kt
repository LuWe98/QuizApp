package com.example.quizapp.model.datastore

import android.security.keystore.KeyProperties
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Singleton
import android.util.Base64
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

@Singleton
class EncryptionUtil
@Inject constructor(
    private val secretKey: String,
    private val salt: String,
    private val initialisationVector: String
) {

    companion object {
        //"AES/GCM/NoPadding" | "AES/CBC/PKCS7Padding"
        private const val TRANSFORMATION = "AES/CBC/PKCS7Padding"
        private const val ALGORITHM = "PBKDF2WithHmacSHA1"
        private const val ITERATION_COUNT = 5_000
        private const val KEY_LENGTH = 256
        private const val BASE_64_FLAGS = Base64.DEFAULT
    }

    fun encrypt(strToEncrypt: String): String {
        val bytes = initCipher(Cipher.ENCRYPT_MODE, strToEncrypt.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(bytes, BASE_64_FLAGS)
    }

    fun decrypt(strToDecrypt: String): String {
        val bytes = initCipher(Cipher.DECRYPT_MODE, Base64.decode(strToDecrypt, BASE_64_FLAGS))
        return String(bytes)
    }

    @ExperimentalSerializationApi
    inline fun <reified T> encryptObject(value: T) = encrypt(Json.encodeToString(value))

    @ExperimentalSerializationApi
    inline fun <reified T> decryptToObject(strToDecrypt: String) = Json.decodeFromString<T>(decrypt(strToDecrypt))


    private fun initCipher(cipherMode: Int, input: ByteArray) = Cipher.getInstance(TRANSFORMATION).run {
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val pbeKeySpec = PBEKeySpec(secretKey.toCharArray(), Base64.decode(salt, BASE_64_FLAGS), ITERATION_COUNT, KEY_LENGTH)
        val generatedSecretKey = factory.generateSecret(pbeKeySpec)
        val secretKeySpec = SecretKeySpec(generatedSecretKey.encoded, KeyProperties.KEY_ALGORITHM_AES)
        val ivParamSpec = IvParameterSpec(Base64.decode(initialisationVector, BASE_64_FLAGS))

        init(cipherMode, secretKeySpec, ivParamSpec)
        doFinal(input)
    }
}