package com.lucasmdjl.application.plugins

import com.lucasmdjl.application.dto.SessionDto
import com.lucasmdjl.application.sessionService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*


fun Application.installAuthentication() {
    install(Authentication) {
        session<SessionDto>("session-auth") {
            validate { session ->
                if (sessionService.getById(session.sessionId) != null) session
                else null
            }
            challenge {
                call.respondRedirect("/")
            }
        }
    }
}