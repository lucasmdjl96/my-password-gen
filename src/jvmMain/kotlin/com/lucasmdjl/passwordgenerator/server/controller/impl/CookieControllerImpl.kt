package com.lucasmdjl.passwordgenerator.server.controller.impl

import com.lucasmdjl.passwordgenerator.common.routes.CookieRoute
import com.lucasmdjl.passwordgenerator.server.controller.CookieController
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.service.SessionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

class CookieControllerImpl(private val sessionService: SessionService) :
    CookieController {

    override suspend fun get(call: ApplicationCall, cookieRoute: CookieRoute.OptOut) {
        val sessionDto = call.sessions.get<SessionDto>()
        if (sessionDto != null) {
            sessionService.delete(sessionDto)
            call.sessions.clear<SessionDto>()
        }
        call.respondText(
            this::class.java.classLoader.getResource("opt-out.html")!!.readText(),
            ContentType.Text.Html
        )
    }

    override suspend fun get(call: ApplicationCall, cookieRoute: CookieRoute.Policy) {
        call.respondText(
            this::class.java.classLoader.getResource("policy.html")!!.readText(),
            ContentType.Text.Html
        )
    }

}
