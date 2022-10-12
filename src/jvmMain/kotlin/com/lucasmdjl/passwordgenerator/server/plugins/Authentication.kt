package com.lucasmdjl.passwordgenerator.server.plugins

import com.lucasmdjl.passwordgenerator.server.controller.SessionController
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import io.ktor.server.application.*
import io.ktor.server.auth.*
import mu.KotlinLogging
import org.koin.ktor.ext.inject

private val logger = KotlinLogging.logger("Authentication")

fun Application.installAuthentication() {

    val sessionController by inject<SessionController>()

    pluginLogger.debug { "Installing Authentication" }
    install(Authentication) {
        session<SessionDto>("session-auth") {
            validate { sessionDto ->
                logger.debug { "validate" }
                sessionController.validate(this, sessionDto)
            }
            challenge { sessionDto ->
                logger.debug { "challenge" }
                sessionController.challenge(call, sessionDto)
            }
        }
    }
}
