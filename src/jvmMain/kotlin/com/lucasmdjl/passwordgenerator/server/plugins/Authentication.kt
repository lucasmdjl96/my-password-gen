package com.lucasmdjl.passwordgenerator.server.plugins

import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.sessionService
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
                if (sessionService.find(session) != null) session
                else null
            }
            challenge { session ->
                logger.debug { "challenge call with sessionId: ${session?.sessionId}" }
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}
