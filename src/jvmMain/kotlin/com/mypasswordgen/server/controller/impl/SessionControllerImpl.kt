/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.controller.impl

import com.mypasswordgen.common.dto.fullServer.FullSessionServerDto
import com.mypasswordgen.common.routes.SessionRoute
import com.mypasswordgen.server.controller.SessionController
import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.plugins.NotAuthenticatedException
import com.mypasswordgen.server.service.SessionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

class SessionControllerImpl(
    private val sessionService: SessionService
) : SessionController {

    override suspend fun put(call: ApplicationCall, sessionRoute: SessionRoute.Update) {
        val sessionDto = call.sessions.get<SessionDto>()
        val newSession = sessionService.assignNew(sessionDto)
        call.sessions.set(newSession)
        call.respond(HttpStatusCode.OK)
    }

    override suspend fun get(call: ApplicationCall, sessionRoute: SessionRoute.Export) {
        val sessionDto = call.sessions.get<SessionDto>() ?: throw NotAuthenticatedException()
        val fullSessionClientDto = sessionService.getFullSession(sessionDto)
        call.respond(fullSessionClientDto)
    }

    override suspend fun post(call: ApplicationCall, sessionRoute: SessionRoute.Import) {
        val sessionDto = call.sessions.get<SessionDto>() ?: throw NotAuthenticatedException()
        val fullSessionServerDto = call.receive<FullSessionServerDto>()
        val (newSessionDto, sessionIDBDto) = sessionService.createFullSession(sessionDto, fullSessionServerDto)
        call.sessions.set(newSessionDto)
        call.respond(sessionIDBDto)
    }

    override fun validate(call: ApplicationCall, sessionDto: SessionDto): SessionDto? {
        return if (sessionService.find(sessionDto) != null) sessionDto
        else null
    }

    override suspend fun challenge(call: ApplicationCall, sessionDto: SessionDto?) {
        throw NotAuthenticatedException()
    }

}
