package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.sessionMapper
import com.lucasmdjl.passwordgenerator.server.sessionService
import com.lucasmdjl.passwordgenerator.server.userService
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
        logger.debug { "/: call with sessionId: ${sessionDto?.sessionId}" }
        val newSession = sessionService.create()
        if (sessionDto != null) {
            val oldSession = sessionService.getById(sessionDto.sessionId)
            if (oldSession != null) {
                userService.moveAllUsers(oldSession, newSession)
                sessionService.delete(oldSession)
            }
        }
        call.sessions.set(sessionMapper.sessionToSessionDto(newSession))

        logger.debug { "/: respond with html" }
        call.respondText(
            this::class.java.classLoader.getResource("index.html")!!.readText(),
            ContentType.Text.Html
        )
    }
}
