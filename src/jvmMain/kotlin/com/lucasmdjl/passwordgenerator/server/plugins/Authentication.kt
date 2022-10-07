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
                logger.debug { "validate" }
                if (sessionService.find(session) != null) session
                else null
            }
            challenge {
                logger.debug { "challenge" }
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}
