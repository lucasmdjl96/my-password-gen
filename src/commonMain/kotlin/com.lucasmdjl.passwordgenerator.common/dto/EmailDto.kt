package com.lucasmdjl.passwordgenerator.common.dto

import kotlinx.serialization.Serializable

@Serializable
class EmailDto(val emailAddress: String, val siteList: MutableList<String>) {

    constructor(emailAddress: String) : this(emailAddress, mutableListOf())

    fun hasSite(siteName: String): Boolean = siteList.find { it == siteName } != null

    fun addSite(siteName: String) = siteList.add(siteName)

    fun removeSite(siteName: String) = siteList.remove(siteName)

    override fun toString(): String {
        return "[EmailDto: $emailAddress]"
    }

}
