package com.lucasmdjl.application.plugins

import com.lucasmdjl.application.dto.SessionCookie
import io.ktor.server.application.*
import io.ktor.server.sessions.*


fun Application.installSessions() {
    install(Sessions) {
        cookie<SessionCookie>("session") {
            cookie.maxAgeInSeconds = 30 * 24 * 60 * 60
            cookie.secure = false
            cookie.path = "/"
        }
    }
}