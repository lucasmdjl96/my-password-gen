package com.lucasmdjl.passwordgenerator.common.routes

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/email")
class EmailRoute {

    @Serializable
    @Resource("new")
    class New(val username: String)

    @Serializable
    @Resource("find/{emailAddress}")
    class Find(val emailAddress: String, val username: String)

    @Serializable
    @Resource("delete/{emailAddress}")
    class Delete(val emailAddress: String, val username: String)

}
