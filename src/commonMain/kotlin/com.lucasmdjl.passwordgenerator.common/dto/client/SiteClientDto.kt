package com.lucasmdjl.passwordgenerator.common.dto.client

import kotlinx.serialization.Serializable

@Serializable
class SiteClientDto(val siteName: String) {

    override fun toString(): String {
        return "[SiteDto: $siteName]"
    }

}
