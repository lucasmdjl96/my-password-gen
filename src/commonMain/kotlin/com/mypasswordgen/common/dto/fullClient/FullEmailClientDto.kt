package com.mypasswordgen.common.dto.fullClient

import kotlinx.serialization.Serializable

@Serializable
data class FullEmailClientDto(val id: String, val sites: List<FullSiteClientDto>) {
    constructor(id: String, builderBlock: Builder.() -> Unit = {}) : this(id, Builder().apply(builderBlock).siteList)
    class Builder {
        private val sites: MutableList<FullSiteClientDto> = mutableListOf()
        val siteList: List<FullSiteClientDto>
            get() = sites.toList()
        operator fun FullSiteClientDto.unaryPlus() = sites.add(this)
    }
}
