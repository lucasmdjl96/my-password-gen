package com.lucasmdjl.application.plugins.routing

import com.lucasmdjl.application.dto.SessionDto
import com.lucasmdjl.application.sessionMapper
import com.lucasmdjl.application.sessionService
import com.lucasmdjl.application.userService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger("MainRoute")

fun Route.mainRoute() {
    get("/") {
        val sessionDto = call.sessions.get<SessionDto>()
        val newSession = sessionService.create()
        if (sessionDto != null) {
            val oldSession = sessionService.getById(sessionDto.sessionId)
            if (oldSession != null) {
                userService.moveAllUsers(oldSession, newSession)
                sessionService.delete(oldSession)
            }
        }
        call.sessions.set(sessionMapper.sessionToSessionDto(newSession))
        call.respondText(
            this::class.java.classLoader.getResource("index.html")!!.readText(),
            ContentType.Text.Html
        )
    }
}