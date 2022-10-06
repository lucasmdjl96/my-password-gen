package com.lucasmdjl.passwordgenerator.common.dto.server

import kotlinx.serialization.Serializable

@Serializable
data class EmailServerDto(val emailAddress: String, val userServerDto: UserServerDto) {
    constructor(emailAddress: String, username: String) : this(emailAddress, UserServerDto(username))
}
