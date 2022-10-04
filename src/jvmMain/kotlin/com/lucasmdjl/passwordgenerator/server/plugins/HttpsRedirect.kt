package com.lucasmdjl.passwordgenerator.server.plugins


import io.ktor.server.application.*
import io.ktor.server.plugins.httpsredirect.*

fun Application.installHttpsRedirect() {
    install(HttpsRedirect) {
        sslPort = 8443
        permanentRedirect = true
    }
}