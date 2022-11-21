package com.mypasswordgen.common.dto.fullServer

import kotlinx.serialization.Serializable

@Serializable
data class FullEmailServerDto(val emailAddress: String, val sites: MutableList<FullSiteServerDto>) {
    constructor(emailAddress: String, builderBlock: Builder.() -> Unit = {}) : this(emailAddress, Builder().apply(builderBlock).sites)
    fun addSite(site: FullSiteServerDto) = sites.add(site)
    class Builder {
        val sites: MutableList<FullSiteServerDto> = mutableListOf()
        val siteList: List<FullSiteServerDto>
            get() = sites.toList()
        operator fun FullSiteServerDto.unaryPlus() = sites.add(this)
    }
}
