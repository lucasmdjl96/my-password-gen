package com.lucasmdjl.application.plugins

import com.lucasmdjl.application.dto.SessionDto
import com.lucasmdjl.application.sessionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger("Authentication")

fun Application.installAuthentication() {
    install(Authentication) {
        session<SessionDto>("session-auth") {
            validate { session ->
                logger.debug { "validate call with sessionId: ${session.sessionId}" }
                if (sessionService.getById(session.sessionId) != null) session
                else null
            }
            challenge { session ->
                logger.debug { "challenge call with sessionId: ${session?.sessionId}" }
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}