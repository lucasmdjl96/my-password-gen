package com.lucasmdjl.passwordgenerator.common.routes

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/user")
class UserRoute {

    @Serializable
    @Resource("/login")
    class Login(val parent: UserRoute = UserRoute())

    @Serializable
    @Resource("/register")
    class Register(val parent: UserRoute = UserRoute())

}
