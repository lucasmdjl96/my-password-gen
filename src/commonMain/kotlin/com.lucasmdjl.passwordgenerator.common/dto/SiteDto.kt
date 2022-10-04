package com.lucasmdjl.passwordgenerator.common.dto

import kotlinx.serialization.Serializable

@Serializable
class SiteDto(val siteName: String) {

    override fun toString(): String {
        return "[SiteDto: $siteName]"
    }

}