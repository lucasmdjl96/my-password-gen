package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.routes.CookieRoute
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Route.cookieRoutes() {
    get<CookieRoute.OptOut> {
        call.sessions.clear<SessionDto>()
        call.respondText(
            this::class.java.classLoader.getResource("opt-out.html")!!.readText(),
            ContentType.Text.Html
        )
    }
    get<CookieRoute.Policy> {
        call.respondText(
            this::class.java.classLoader.getResource("policy.html")!!.readText(),
            ContentType.Text.Html
        )
    }
}
