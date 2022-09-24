package com.lucasmdjl.application.routes

import SessionCookie
import com.lucasmdjl.application.sessionService
import com.lucasmdjl.application.sha256
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import java.util.*

fun Application.installRoutes() {
    routing {
        mainRoute()
        sessionRoutes()
        userRoutes()
        emailRoutes()
        siteRoutes()
        static("/static") {
            resources()
        }
    }
}

fun Routing.mainRoute() {
    get("/password/{username}/{emailAddress}/{siteName}") {
        val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
        val sessionDto = sessionService.getById(sessionId)!!
        call.respondText(
            sha256(
                """
                    ${call.parameters.getOrFail("username")}
                    ${call.parameters.getOrFail("emailAddress")}
                    ${call.parameters.getOrFail("siteName")}
                    ${sessionDto.password}
                """.trimIndent()
            )
        )
    }
}