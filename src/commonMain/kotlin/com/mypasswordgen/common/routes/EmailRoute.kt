package com.mypasswordgen.common.routes

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/email")
class EmailRoute {

    @Serializable
    @Resource("/new")
    class New(val parent: EmailRoute = EmailRoute())

    @Serializable
    @Resource("/find/{emailAddress}")
    class Find(val emailAddress: String, val parent: EmailRoute = EmailRoute())

    @Serializable
    @Resource("/delete/{emailAddress}")
    class Delete(val emailAddress: String, val parent: EmailRoute = EmailRoute())

}
