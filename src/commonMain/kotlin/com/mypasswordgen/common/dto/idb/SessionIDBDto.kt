package com.mypasswordgen.common.dto.idb

import kotlinx.serialization.Serializable

@Serializable
data class SessionIDBDto(val users: List<UserIDBDto>) {
    constructor(builderBlock: Builder.() -> Unit = {}) : this(Builder().apply(builderBlock).userList)
    class Builder {
        private val users: MutableList<UserIDBDto> = mutableListOf()
        val userList: List<UserIDBDto>
            get() = users.toList()
        operator fun UserIDBDto.unaryPlus() = users.add(this)
    }
}
