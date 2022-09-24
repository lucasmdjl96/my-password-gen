package com.lucasmdjl.application.routes

import SessionCookie
import com.lucasmdjl.application.emailService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import java.util.*

fun Routing.emailRoutes() {
    post("/new/email/{username}") {
        val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
        val userDto = emailService.addEmailToUser(
            call.receiveText().trim('"'),
            call.parameters.getOrFail("username"),
            sessionId
        )
        call.respond(userDto)
    }
    post("/find/email/{username}") {
        val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
        val emailDto = emailService.getEmailFromUser(
            call.receiveText().trim('"'),
            call.parameters.getOrFail("username"),
            sessionId
        )
        call.respondNullable(emailDto)
    }
}