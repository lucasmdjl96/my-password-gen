package com.lucasmdjl.passwordgenerator.common.routes

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/user")
class UserRoute {

    @Serializable
    @Resource("login")
    class Login

    @Serializable
    @Resource("register")
    class Register

}
