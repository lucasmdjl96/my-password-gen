package com.mypasswordgen.common.dto.fullServer

import kotlinx.serialization.Serializable

@Serializable
data class FullSessionServerDto(val users: MutableList<FullUserServerDto>) {
    constructor(builderBlock: Builder.() -> Unit = {}) : this(Builder().apply(builderBlock).users)
    fun addUser(user: FullUserServerDto) = users.add(user)
    class Builder {
        val users: MutableList<FullUserServerDto> = mutableListOf()
        val userList: List<FullUserServerDto>
            get() = users.toList()
        operator fun FullUserServerDto.unaryPlus() = users.add(this)
    }
}
