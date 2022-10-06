package com.lucasmdjl.passwordgenerator.server.crypto

import com.lucasmdjl.passwordgenerator.common.dto.server.UserServerDto
import java.security.MessageDigest
import java.util.*

private fun sha256(message: String): String {
    val input = message.toByteArray()
    val bytes = MessageDigest.getInstance("SHA-256").digest(input)
    return Base64.getUrlEncoder().encodeToString(bytes)
}

private fun String.encode() = sha256(this)

fun UserServerDto.encode() = UserServerDto(this.username.encode())
