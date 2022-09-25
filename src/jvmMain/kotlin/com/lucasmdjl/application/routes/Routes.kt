package com.lucasmdjl.application.routes

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Application.installRoutes() {
    routing {
        mainRoute()
        userRoutes()
        emailRoutes()
        siteRoutes()
        static("/static") {
            resources()
        }
    }
}