package com.mypasswordgen.common.routes

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

    @Serializable
    @Resource("/logout")
    class Logout(val parent: UserRoute = UserRoute())

    @Serializable
    @Resource("/import")
    class Import(val parent: UserRoute = UserRoute())

    @Serializable
    @Resource("/export/{username}")
    class Export(val username: String, val parent: UserRoute = UserRoute())

}
