package com.lucasmdjl.passwordgenerator.common.dto.server

import kotlinx.serialization.Serializable

@Serializable
class SiteServerDto(val siteName: String, val emailAddress: String, val username: String)
