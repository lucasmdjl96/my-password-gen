package com.lucasmdjl.passwordgenerator.server.plugins


import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun Application.installContentNegotiation() {
    pluginLogger.debug { "Installing ContentNegotiation" }
    install(ContentNegotiation) {
        json()
    }
}
