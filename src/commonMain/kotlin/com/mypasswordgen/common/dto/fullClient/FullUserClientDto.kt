package com.mypasswordgen.common.dto.fullClient

import kotlinx.serialization.Serializable

@Serializable
data class FullUserClientDto(val emails: List<FullEmailClientDto>) {
    constructor(builderBlock: Builder.() -> Unit = {}) : this(Builder().apply(builderBlock).emailList)
    class Builder {
        private val emails: MutableList<FullEmailClientDto> = mutableListOf()
        val emailList: List<FullEmailClientDto>
            get() = emails.toList()
        operator fun FullEmailClientDto.unaryPlus() = emails.add(this)
    }
}
