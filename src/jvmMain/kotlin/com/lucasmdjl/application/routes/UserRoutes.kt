package com.lucasmdjl.application.routes

import SessionCookie
import com.lucasmdjl.application.sessionService
import com.lucasmdjl.application.userService
import dto.LoginDto
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.util.*

fun Routing.userRoutes() {
    post("/login") {
        val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
        val login = call.receive<LoginDto>()
        sessionService.updatePasswordById(sessionId, login.password)
        val userDto = userService.getByName(
            login.username,
            sessionId
        )
        call.respondNullable(userDto)
    }
    post("/new/user") {
        val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
        val login = call.receive<LoginDto>()
        sessionService.updatePasswordById(sessionId, login.password)
        val userDto = userService.create(
            login.username,
            sessionId
        )
        call.respondNullable(userDto)
    }
}