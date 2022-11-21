package com.mypasswordgen.server.repository.crypto

import java.security.MessageDigest
import java.util.*

private fun sha256(message: String): String {
    val input = message.toByteArray()
    val bytes = MessageDigest.getInstance("SHA3-256").digest(input)
    return Base64.getUrlEncoder().encodeToString(bytes)
}

fun String.encode() = sha256(this)
