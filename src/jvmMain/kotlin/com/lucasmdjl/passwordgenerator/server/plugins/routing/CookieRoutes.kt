package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.routes.CookieRoute
import com.lucasmdjl.passwordgenerator.server.controller.CookieController
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.cookieRoutes() {

    val cookieController by inject<CookieController>()

    get<CookieRoute.OptOut>(cookieController::get)
    get<CookieRoute.Policy>(cookieController::get)

}
