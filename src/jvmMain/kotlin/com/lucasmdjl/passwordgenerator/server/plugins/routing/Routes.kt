package com.lucasmdjl.passwordgenerator.server.plugins.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*

import io.ktor.server.routing.*

fun Application.installRoutes() {
    routing {
        mainRoute()
        sessionRoutes()
        authenticate("session-auth") {
            userRoutes()
            emailRoutes()
            siteRoutes()
        }
        static("/static") {
            resources()
        }
    }
}
