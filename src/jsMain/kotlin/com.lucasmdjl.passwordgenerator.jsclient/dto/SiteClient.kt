package com.lucasmdjl.passwordgenerator.jsclient.dto

import kotlinx.serialization.Serializable

@Serializable
class SiteClient(val siteName: String) {

    override fun toString(): String {
        return "[SiteDto: $siteName]"
    }

}
