package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.routes.SessionRoute
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.mapper.impl.SessionMapperImpl.toSessionDto
import com.lucasmdjl.passwordgenerator.server.sessionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger("SessionRoutes")

fun Route.sessionRoutes() {
    put<SessionRoute> {
        logger.debug { call.request.path() }
        val sessionDto = call.sessions.get<SessionDto>()
        val newSession = sessionService.assignNew(sessionDto)
        call.sessions.set(newSession.toSessionDto())
        call.respond(HttpStatusCode.OK)
    }
}
