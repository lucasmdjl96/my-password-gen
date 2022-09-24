package com.lucasmdjl.application.routes

import SessionCookie
import com.lucasmdjl.application.sessionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.util.*

fun Routing.sessionRoutes() {
    get("/") {
        val sessionCookieTemp = call.sessions.get<SessionCookie>()
        val sessionDto = if (sessionCookieTemp != null) {
            sessionService.getById(UUID.fromString(sessionCookieTemp.sessionId)) ?: sessionService.create()
        } else {
            sessionService.create()
        }
        sessionService.updatePasswordById(sessionDto.sessionId, null)
        call.sessions.set(SessionCookie(sessionDto.sessionId.toString()))
        //call.respondHtml(HttpStatusCode.OK, HTML::index)
        call.respondText(
            this::class.java.classLoader.getResource("index.html")!!.readText(),
            ContentType.Text.Html
        )
    }
    get("/logout") {
        val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
        sessionService.updatePasswordById(sessionId, null)
        call.respond(HttpStatusCode.OK)
    }
}