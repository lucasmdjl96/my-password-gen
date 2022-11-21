package com.mypasswordgen.common.dto.fullClient

import kotlinx.serialization.Serializable

@Serializable
data class FullSessionClientDto(val users: List<FullUserClientDto>) {
    constructor(builderBlock: Builder.() -> Unit = {}) : this(Builder().apply(builderBlock).userList)
    class Builder {
        private val users: MutableList<FullUserClientDto> = mutableListOf()
        val userList: List<FullUserClientDto>
            get() = users.toList()
        operator fun FullUserClientDto.unaryPlus() = users.add(this)
    }
}
