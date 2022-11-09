package com.mypasswordgen.server.controller.impl

import com.mypasswordgen.common.routes.SessionRoute
import com.mypasswordgen.server.controller.SessionController
import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.mapper.SessionMapper
import com.mypasswordgen.server.plugins.NotAuthenticatedException
import com.mypasswordgen.server.service.SessionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

class SessionControllerImpl(
    private val sessionService: SessionService,
    private val sessionMapper: SessionMapper
) : SessionController {

    override suspend fun put(call: ApplicationCall, sessionRoute: SessionRoute) {
        val sessionDto = call.sessions.get<SessionDto>()
        val newSession = sessionService.assignNew(sessionDto)
        call.sessions.set(with(sessionMapper) { newSession.toSessionDto() })
        call.respond(HttpStatusCode.OK)
    }

    override fun validate(call: ApplicationCall, sessionDto: SessionDto): SessionDto? {
        return if (sessionService.find(sessionDto) != null) sessionDto
        else null
    }

    override suspend fun challenge(call: ApplicationCall, sessionDto: SessionDto?) {
        throw NotAuthenticatedException()
    }

}
