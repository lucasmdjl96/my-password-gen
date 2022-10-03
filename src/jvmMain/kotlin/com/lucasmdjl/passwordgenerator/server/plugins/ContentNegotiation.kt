package com.lucasmdjl.passwordgenerator.server.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*

import io.ktor.server.plugins.contentnegotiation.*

fun Application.installContentNegotiation() {
    install(ContentNegotiation) {
        json()
    }
}
