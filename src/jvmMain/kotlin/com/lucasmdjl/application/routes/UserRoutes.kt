package com.lucasmdjl.application.routes

import com.lucasmdjl.application.userService
import dto.SessionCookie
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.util.*

fun Routing.userRoutes() {
    route("/user") {
        post("/login") {
            val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
            val username = call.receiveText().trim('"')
            val userDto = userService.getByName(
                username,
                sessionId
            )
            call.respondNullable(userDto)
        }
        post("/register") {
            val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
            val username = call.receiveText().trim('"')
            val userDto = userService.create(
                username,
                sessionId
            )
            call.respondNullable(userDto)
        }
    }
}