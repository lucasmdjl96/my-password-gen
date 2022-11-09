package com.mypasswordgen.server.plugins.routing

import com.mypasswordgen.common.routes.CookieRoute
import com.mypasswordgen.server.controller.CookieController
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.cookieRoutes() {

    val cookieController by inject<CookieController>()

    get<CookieRoute.OptOut>(cookieController::get)
    get<CookieRoute.Policy>(cookieController::get)

}
