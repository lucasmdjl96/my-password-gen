package com.lucasmdjl.passwordgenerator.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*

import io.ktor.server.plugins.cors.routing.*


fun Application.installCORS() {
    pluginLogger.debug { "Installing CORS" }
    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        anyHost()
    }
}
