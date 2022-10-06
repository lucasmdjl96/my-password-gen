package com.lucasmdjl.passwordgenerator.common.dto.server

import kotlinx.serialization.Serializable

@Serializable
data class SiteServerDto(val siteName: String, val emailServerDto: EmailServerDto) {
    constructor(siteName: String, emailAddress: String, username: String) : this(
        siteName,
        EmailServerDto(emailAddress, username)
    )
}
