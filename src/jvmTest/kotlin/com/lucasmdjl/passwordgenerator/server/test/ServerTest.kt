package com.lucasmdjl.passwordgenerator.server.test

import com.lucasmdjl.passwordgenerator.server.plugins.*
import com.lucasmdjl.passwordgenerator.server.plugins.routing.installRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.module() {
    installKoin()
    installContentNegotiation()
    installCORS()
    installCompression()
    installSessions()
    installAuthentication()
    installHttpsRedirect()
    installResources()
    installRoutes()
    installCallLogging()
    routing {
        authenticate("session-auth") {
            get("/test-authentication") {
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
