package com.lucasmdjl.passwordgenerator.server.plugins

import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import io.ktor.server.application.*

import io.ktor.server.sessions.*


fun Application.installSessions() {
    install(Sessions) {
        cookie<SessionDto>("session") {
            cookie.maxAgeInSeconds = 30 * 24 * 60 * 60
            cookie.secure = true
            cookie.path = "/"
        }
    }
}
