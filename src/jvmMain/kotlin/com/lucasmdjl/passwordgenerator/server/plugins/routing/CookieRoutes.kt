package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.routes.CookieRoute
import com.lucasmdjl.passwordgenerator.server.controller.CookieController
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import org.koin.ktor.ext.inject

private val logger = KotlinLogging.logger("CookieRoutes")

fun Route.cookieRoutes() {

    val cookieController by inject<CookieController>()

    get<CookieRoute.OptOut> { cookieRoute ->
        logger.debug { call.request.path() }
        cookieController.get(call, cookieRoute)
    }
    get<CookieRoute.Policy> { cookieRoute ->
        logger.debug { call.request.path() }
        cookieController.get(call, cookieRoute)
    }

}
