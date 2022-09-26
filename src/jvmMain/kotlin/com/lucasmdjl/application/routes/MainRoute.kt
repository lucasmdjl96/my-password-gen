package com.lucasmdjl.application.routes

import com.lucasmdjl.application.dto.SessionCookie
import com.lucasmdjl.application.sessionService
import com.lucasmdjl.application.userService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.util.*

fun Route.mainRoute() {
    get("/") {
        val sessionCookie = call.sessions.get<SessionCookie>()
        val sessionDto = sessionService.create()
        if (sessionCookie != null && sessionService.getById(UUID.fromString(sessionCookie.sessionId)) != null) {
            userService.moveAllUsers(UUID.fromString(sessionCookie.sessionId), sessionDto.sessionId)
            sessionService.deleteById(UUID.fromString(sessionCookie.sessionId))
        }
        call.sessions.set(SessionCookie(sessionDto.sessionId.toString()))
        call.respondText(
            this::class.java.classLoader.getResource("index.html")!!.readText(),
            ContentType.Text.Html
        )
    }
}