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

import com.mypasswordgen.common.dto.server.EmailServerDto
import com.mypasswordgen.common.routes.EmailRoute
import com.mypasswordgen.server.controller.EmailController
import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.plugins.NotAuthenticatedException
import com.mypasswordgen.server.service.EmailService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

class EmailControllerImpl(
    private val emailService: EmailService
) : EmailController {

    override suspend fun post(call: ApplicationCall, emailRoute: EmailRoute.New) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val emailServerDto = call.receive<EmailServerDto>()
        val emailClientDto = emailService.create(emailServerDto, sessionId)
        call.respond(emailClientDto)
    }

    override suspend fun get(call: ApplicationCall, emailRoute: EmailRoute.Find) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val emailServerDto = EmailServerDto(emailRoute.emailAddress)
        val emailClientDto = emailService.find(emailServerDto, sessionId)
        call.respond(emailClientDto)
    }

    override suspend fun delete(call: ApplicationCall, emailRoute: EmailRoute.Delete) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val emailServerDto = EmailServerDto(emailRoute.emailAddress)
        val emailClientDto = emailService.delete(emailServerDto, sessionId)
        call.respond(emailClientDto)
    }

}
