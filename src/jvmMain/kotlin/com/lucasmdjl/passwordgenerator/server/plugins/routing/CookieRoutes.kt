package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.routes.CookieRoute
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.sessionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger("CookieRoutes")

fun Route.cookieRoutes() {
    get<CookieRoute.OptOut> {
        logger.debug { call.request.path() }
        val sessionDto = call.sessions.get<SessionDto>()
        if (sessionDto != null) sessionService.delete(sessionDto)
        call.sessions.clear<SessionDto>()
        call.respondText(
            this::class.java.classLoader.getResource("opt-out.html")!!.readText(),
            ContentType.Text.Html
        )
    }
    get<CookieRoute.Policy> {
        logger.debug { call.request.path() }
        call.respondText(
            this::class.java.classLoader.getResource("policy.html")!!.readText(),
            ContentType.Text.Html
        )
    }
}
