package com.mypasswordgen.common.dto.fullServer

import kotlinx.serialization.Serializable

@Serializable
data class FullUserServerDto(val username: String, val emails: MutableList<FullEmailServerDto>) {
    constructor(username: String, builderBlock: Builder.() -> Unit = {}) : this(username, Builder().apply(builderBlock).emails)
    fun addEmail(email: FullEmailServerDto) = emails.add(email)
    class Builder {
        val emails: MutableList<FullEmailServerDto> = mutableListOf()
        val emailList: List<FullEmailServerDto>
            get() = emails.toList()
        operator fun FullEmailServerDto.unaryPlus() = emails.add(this)
    }
}
