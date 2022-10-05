package com.lucasmdjl.passwordgenerator.common.dto.server

import kotlinx.serialization.Serializable

@Serializable
class EmailServerDto(val emailAddress: String, val username: String)
