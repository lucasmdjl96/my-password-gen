package com.mypasswordgen.jsclient.dto

import kotlinx.serialization.Serializable

@Serializable
class SiteClient(val siteName: String) {

    override fun toString(): String {
        return "[SiteDto: $siteName]"
    }

}
