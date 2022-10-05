package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.mapper.impl.SessionMapperImpl.toSessionDto
import com.lucasmdjl.passwordgenerator.server.sessionService
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
        val newSession = sessionService.assignNew(sessionDto)
        call.sessions.set(newSession.toSessionDto())

        logger.debug { "/: respond with html" }
        call.respondText(
            this::class.java.classLoader.getResource("index.html")!!.readText(),
            ContentType.Text.Html
        )
    }
}
