package com.mypasswordgen.common.dto.idb

import kotlinx.serialization.Serializable

@Serializable
data class UserIDBDto(val emails: List<EmailIDBDto>) {
    constructor(builderBlock: Builder.() -> Unit = {}) : this(Builder().apply(builderBlock).emailList)
    class Builder {
        private val emails: MutableList<EmailIDBDto> = mutableListOf()
        val emailList: List<EmailIDBDto>
            get() = emails.toList()
        operator fun EmailIDBDto.unaryPlus() = emails.add(this)
    }
}
