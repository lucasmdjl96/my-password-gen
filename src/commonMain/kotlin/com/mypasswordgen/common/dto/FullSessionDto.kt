package com.mypasswordgen.common.dto

import kotlinx.serialization.Serializable

@Serializable
class FullSessionClientDto(val users: MutableList<FullUserClientDto> = mutableListOf())

@Serializable
class FullUserClientDto(val emails: MutableList<FullEmailClientDto> = mutableListOf())

@Serializable
class FullEmailClientDto(val id: String, val sites: MutableList<FullSiteClientDto> = mutableListOf())

@Serializable
class FullSiteClientDto(val id: String)

@Serializable
class FullSessionServerDto(val users: MutableList<FullUserServerDto> = mutableListOf()) {
    fun addUser(user: FullUserServerDto) = users.add(user)
}

@Serializable
class FullUserServerDto(val username: String, val emails: MutableList<FullEmailServerDto> = mutableListOf()) {
    fun addEmail(email: FullEmailServerDto) = emails.add(email)
}

@Serializable
class FullEmailServerDto(val emailAddress: String, val sites: MutableList<FullSiteServerDto> = mutableListOf()) {
    fun addSite(site: FullSiteServerDto) = sites.add(site)
}

@Serializable
class FullSiteServerDto(val siteName: String)

@Serializable
class SessionIDBDto(val users: MutableList<UserIDBDto> = mutableListOf())

@Serializable
class UserIDBDto(val emails: MutableList<EmailIDBDto> = mutableListOf())

@Serializable
class EmailIDBDto(val id: String, val emailAddress: String, val sites: MutableList<SiteIDBDto> = mutableListOf())

@Serializable
class SiteIDBDto(val id: String, val siteName: String)
