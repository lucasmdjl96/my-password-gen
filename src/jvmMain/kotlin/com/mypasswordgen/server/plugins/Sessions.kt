package com.mypasswordgen.server.plugins

import com.mypasswordgen.server.dto.SessionDto
import io.ktor.server.application.*

import io.ktor.server.sessions.*


fun Application.installSessions() {
    pluginLogger.debug { "Installing Sessions" }
    install(Sessions) {
        cookie<SessionDto>("session") {
            cookie.maxAgeInSeconds = 366 * 24 * 60 * 60
            cookie.secure = true
            cookie.path = "/"
            cookie.httpOnly = true
        }
    }
}
