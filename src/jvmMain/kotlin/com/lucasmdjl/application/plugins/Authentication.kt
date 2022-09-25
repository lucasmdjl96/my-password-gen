package com.lucasmdjl.application.plugins

import com.lucasmdjl.application.dto.SessionCookie
import com.lucasmdjl.application.sessionService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import java.util.*


fun Application.installAuthentication() {
    install(Authentication) {
        session<SessionCookie>("session-auth") {
            validate { session ->
                if (sessionService.getById(UUID.fromString(session.sessionId)) != null) session
                else null
            }
            challenge {
                call.respondRedirect("/")
            }
        }
    }
}