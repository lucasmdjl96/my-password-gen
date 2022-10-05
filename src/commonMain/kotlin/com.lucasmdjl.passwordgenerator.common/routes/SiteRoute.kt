package com.lucasmdjl.passwordgenerator.common.routes

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/site")
class SiteRoute {

    @Serializable
    @Resource("/new")
    class New(val parent: SiteRoute = SiteRoute())

    @Serializable
    @Resource("/find/{siteName}/{emailAddress}/{username}")
    class Find(val parent: SiteRoute, val siteName: String, val emailAddress: String, val username: String) {
        constructor(siteName: String, emailAddress: String, username: String) :
                this(SiteRoute(), siteName, emailAddress, username)
    }

    @Serializable
    @Resource("/delete/{siteName}/{emailAddress}/{username}")
    class Delete(val parent: SiteRoute, val siteName: String, val emailAddress: String, val username: String) {
        constructor(siteName: String, emailAddress: String, username: String) :
                this(SiteRoute(), siteName, emailAddress, username)
    }

}
