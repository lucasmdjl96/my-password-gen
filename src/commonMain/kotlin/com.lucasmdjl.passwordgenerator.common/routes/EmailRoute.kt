package com.lucasmdjl.passwordgenerator.common.routes

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/email")
class EmailRoute {

    @Serializable
    @Resource("/new")
    class New(val parent: EmailRoute = EmailRoute())

    @Serializable
    @Resource("/find/{emailAddress}/{username}")
    class Find(val parent: EmailRoute, val emailAddress: String, val username: String) {
        constructor(emailAddress: String, username: String) : this(EmailRoute(), emailAddress, username)
    }

    @Serializable
    @Resource("/delete/{emailAddress}/{username}")
    class Delete(val parent: EmailRoute, val emailAddress: String, val username: String) {
        constructor(emailAddress: String, username: String) : this(EmailRoute(), emailAddress, username)
    }

}
