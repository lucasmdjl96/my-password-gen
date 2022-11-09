package com.mypasswordgen.server.controller

import com.mypasswordgen.common.routes.CookieRoute
import io.ktor.server.application.*

interface CookieController {

    suspend fun get(call: ApplicationCall, cookieRoute: CookieRoute.OptOut)

    suspend fun get(call: ApplicationCall, cookieRoute: CookieRoute.Policy)

}
