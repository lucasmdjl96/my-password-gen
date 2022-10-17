package com.lucasmdjl.passwordgenerator.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.installStatusPages() {
    install(StatusPages) {
        exception<DataNotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound)
        }
        exception<DataConflictException> { call, cause ->
            call.respond(HttpStatusCode.Conflict)
        }
        exception<NotAuthenticatedException> { call, cause ->
            call.respond(HttpStatusCode.Unauthorized)
        }
        exception<NotEnoughInformationException> { call, cause ->
            call.respond(HttpStatusCode.PreconditionFailed)
        }
    }
}

class DataNotFoundException : RuntimeException()

class DataConflictException : RuntimeException()

class NotAuthenticatedException : RuntimeException()

class NotEnoughInformationException : RuntimeException()
