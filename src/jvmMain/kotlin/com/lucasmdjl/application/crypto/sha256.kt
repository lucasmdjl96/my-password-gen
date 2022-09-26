package com.lucasmdjl.application.crypto

import java.security.MessageDigest
import java.util.*

fun sha256(message: String): String {
    val input = message.toByteArray()
    val bytes = MessageDigest.getInstance("SHA-256").digest(input)
    return Base64.getUrlEncoder().encodeToString(bytes)
}

fun String.encode() = sha256(this)