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
    @Resource("/find/{siteName}")
    class Find(val siteName: String, val parent: SiteRoute = SiteRoute())

    @Serializable
    @Resource("/delete/{siteName}")
    class Delete(val siteName: String, val parent: SiteRoute = SiteRoute())

}
