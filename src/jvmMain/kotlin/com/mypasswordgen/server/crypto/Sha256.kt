package com.mypasswordgen.server.crypto

import com.mypasswordgen.common.dto.FullEmailServerDto
import com.mypasswordgen.common.dto.FullSessionServerDto
import com.mypasswordgen.common.dto.FullSiteServerDto
import com.mypasswordgen.common.dto.FullUserServerDto
import com.mypasswordgen.common.dto.server.EmailServerDto
import com.mypasswordgen.common.dto.server.SiteServerDto
import com.mypasswordgen.common.dto.server.UserServerDto
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

fun FullSiteServerDto.encode() = FullSiteServerDto(this.siteName.encode())

fun FullEmailServerDto.encode() = FullEmailServerDto(this.emailAddress.encode()).apply {
    sites.addAll(sites.map(FullSiteServerDto::encode))
}

fun FullUserServerDto.encode() = FullUserServerDto(this.username.encode()).apply {
    emails.addAll(emails.map(FullEmailServerDto::encode))
}

fun FullSessionServerDto.encode() = FullSessionServerDto().apply {
    users.addAll(users.map(FullUserServerDto::encode))
}
