package com.mypasswordgen.server.plugins

import com.mypasswordgen.server.dto.SessionDto
import io.ktor.server.application.*

import io.ktor.server.sessions.*

private const val cookieDuration: Long = (366 * 24 + 6) * 60 * 60 // 365.25 and 1 days

fun Application.installSessions() {
    pluginLogger.debug { "Installing Sessions" }
    install(Sessions) {
        cookie<SessionDto>("session") {
            cookie.maxAgeInSeconds = cookieDuration
            cookie.secure = true
            cookie.path = "/"
            cookie.httpOnly = true
        }
    }
}
