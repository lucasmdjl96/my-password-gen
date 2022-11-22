package com.mypasswordgen.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.installStatusPages() {
    install(StatusPages) {
        exception<DataNotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, cause.message)
        }
        exception<DataConflictException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, cause.message)
        }
        exception<NotAuthenticatedException> { call, cause ->
            call.respond(HttpStatusCode.Unauthorized, cause.message)
        }
        exception<NotEnoughInformationException> { call, cause ->
            call.respond(HttpStatusCode.PreconditionFailed, cause.message)
        }
    }
}

class DataNotFoundException(override val message: String = "") : RuntimeException()

class DataConflictException(override val message: String = "") : RuntimeException()

class NotAuthenticatedException(override val message: String = "Session not found.") : RuntimeException()

class NotEnoughInformationException(override val message: String = "") : RuntimeException()
