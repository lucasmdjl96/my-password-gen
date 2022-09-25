package com.lucasmdjl.application.routes

import com.lucasmdjl.application.dto.SessionCookie
import com.lucasmdjl.application.sessionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.util.*

fun Route.mainRoute() {
    get("/") {
        val sessionCookie = call.sessions.get<SessionCookie>()
        val sessionDto = if (sessionCookie != null) {
            sessionService.getById(UUID.fromString(sessionCookie.sessionId)) ?: sessionService.create()
        } else {
            sessionService.create()
        }
        call.sessions.set(SessionCookie(sessionDto.sessionId.toString()))
        call.respondText(
            this::class.java.classLoader.getResource("index.html")!!.readText(),
            ContentType.Text.Html
        )
    }
}