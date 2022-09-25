package com.lucasmdjl.application.routes

import com.lucasmdjl.application.dto.SessionCookie
import com.lucasmdjl.application.emailService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import java.util.*

fun Route.emailRoutes() {
    route("/email") {
        post("/new") {
            val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
            val userDto = emailService.addEmailToUser(
                call.receiveText().trim('"'),
                call.request.queryParameters.getOrFail("username"),
                sessionId
            )
            call.respond(userDto)
        }
        get("/find/{emailAddress}") {
            val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
            val emailDto = emailService.getEmailFromUser(
                call.parameters.getOrFail("emailAddress"),
                call.request.queryParameters.getOrFail("username"),
                sessionId
            )
            call.respondNullable(emailDto)
        }
        delete("/delete/{emailAddress}") {
            val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
            val userDto = emailService.removeEmailFromUser(
                call.parameters.getOrFail("emailAddress"),
                call.request.queryParameters.getOrFail("username"),
                sessionId
            )
            call.respond(userDto)
        }
    }
}