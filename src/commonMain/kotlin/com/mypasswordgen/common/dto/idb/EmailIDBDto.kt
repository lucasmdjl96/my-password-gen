package com.mypasswordgen.common.dto.idb

import kotlinx.serialization.Serializable

@Serializable
data class EmailIDBDto(val id: String, val emailAddress: String, val sites: List<SiteIDBDto>) {
    constructor(id: String, emailAddress: String, builderBlock: Builder.() -> Unit = {}) : this(id, emailAddress, Builder().apply(builderBlock).siteList)
    class Builder {
        private val sites: MutableList<SiteIDBDto> = mutableListOf()
        val siteList: List<SiteIDBDto>
            get() = sites.toList()
        operator fun SiteIDBDto.unaryPlus() = sites.add(this)
    }
}
