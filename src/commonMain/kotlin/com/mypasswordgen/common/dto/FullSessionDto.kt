package com.mypasswordgen.common.dto

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

@Serializable
data class FullSiteClientDto(val id: String)

@Serializable
data class FullSessionServerDto(val users: List<FullUserServerDto>) {
    constructor(builderBlock: Builder.() -> Unit = {}) : this(Builder().apply(builderBlock).userList)
    class Builder {
        private val users: MutableList<FullUserServerDto> = mutableListOf()
        val userList: List<FullUserServerDto>
            get() = users.toList()
        operator fun FullUserServerDto.unaryPlus() = users.add(this)
    }
}

@Serializable
data class FullUserServerDto(val username: String, val emails: List<FullEmailServerDto>) {
    constructor(username: String, builderBlock: Builder.() -> Unit = {}) : this(username, Builder().apply(builderBlock).emailList)
    class Builder {
        private val emails: MutableList<FullEmailServerDto> = mutableListOf()
        val emailList: List<FullEmailServerDto>
            get() = emails.toList()
        operator fun FullEmailServerDto.unaryPlus() = emails.add(this)
    }
}

@Serializable
data class FullEmailServerDto(val emailAddress: String, val sites: List<FullSiteServerDto>) {
    constructor(emailAddress: String, builderBlock: Builder.() -> Unit = {}) : this(emailAddress, Builder().apply(builderBlock).siteList)
    class Builder {
        private val sites: MutableList<FullSiteServerDto> = mutableListOf()
        val siteList: List<FullSiteServerDto>
            get() = sites.toList()
        operator fun FullSiteServerDto.unaryPlus() = sites.add(this)
    }
}

@Serializable
data class FullSiteServerDto(val siteName: String)

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

@Serializable
data class SiteIDBDto(val id: String, val siteName: String)
