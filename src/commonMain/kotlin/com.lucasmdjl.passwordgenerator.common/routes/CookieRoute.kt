package com.lucasmdjl.passwordgenerator.common.routes

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/cookies")
class CookieRoute {

    @Serializable
    @Resource("/opt-out")
    class OptOut(val parent: CookieRoute = CookieRoute())

    @Serializable
    @Resource("/policy")
    class Policy(val parent: CookieRoute = CookieRoute())

}
