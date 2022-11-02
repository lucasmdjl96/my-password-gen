package com.lucasmdjl.passwordgenerator.server.crypto

import com.lucasmdjl.passwordgenerator.common.dto.server.UserServerDto
import com.lucasmdjl.passwordgenerator.common.dto.server.EmailServerDto
import com.lucasmdjl.passwordgenerator.common.dto.server.SiteServerDto
import java.security.MessageDigest
import java.util.*

private fun sha256(message: String): String {
    val input = message.toByteArray()
    val bytes = MessageDigest.getInstance("SHA3-256").digest(input)
    return Base64.getUrlEncoder().encodeToString(bytes)
}

fun String.encode() = sha256(this)

fun UserServerDto.encode() = UserServerDto(this.username.encode())
fun EmailServerDto.encode() = EmailServerDto(this.emailAddress.encode())
fun SiteServerDto.encode() = SiteServerDto(this.siteName.encode())
