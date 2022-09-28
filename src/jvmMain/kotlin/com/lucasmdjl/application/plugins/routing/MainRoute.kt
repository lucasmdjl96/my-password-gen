package com.lucasmdjl.application.plugins.routing

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
        val newSession = sessionService.create()
        if (sessionCookie != null) {
            val oldSession = sessionService.getById(UUID.fromString(sessionCookie.sessionId))
            if (oldSession != null) {
                userService.moveAllUsers(oldSession, newSession)
                sessionService.delete(oldSession)
            }
        }
        call.sessions.set(SessionCookie(newSession.id.value.toString()))
        call.respondText(
            this::class.java.classLoader.getResource("index.html")!!.readText(),
            ContentType.Text.Html
        )
    }
}