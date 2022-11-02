package com.lucasmdjl.passwordgenerator.common.dto.client

import kotlinx.serialization.Serializable

@Serializable
class EmailClientDto(val id: String, val siteIdList: List<String>)
