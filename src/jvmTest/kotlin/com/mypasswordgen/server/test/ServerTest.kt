package com.mypasswordgen.server.test

import com.mypasswordgen.server.plugins.*
import com.mypasswordgen.server.plugins.routing.installRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.module() {
    installKoin()
    installContentNegotiation()
    installCompression()
    installSessions()
    installAuthentication()
    installStatusPages()
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
