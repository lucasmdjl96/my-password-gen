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

import com.mypasswordgen.common.dto.server.SiteServerDto
import com.mypasswordgen.common.routes.SiteRoute
import com.mypasswordgen.server.controller.SiteController
import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.plugins.NotAuthenticatedException
import com.mypasswordgen.server.service.SiteService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

class SiteControllerImpl(
    private val siteService: SiteService
) : SiteController {

    override suspend fun post(call: ApplicationCall, siteRoute: SiteRoute.New) {
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val siteServerDto = call.receive<SiteServerDto>()
        val siteClientDto = siteService.create(siteServerDto, sessionId)
        call.respond(siteClientDto)
    }

    override suspend fun get(call: ApplicationCall, siteRoute: SiteRoute.Find) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val siteServerDto = SiteServerDto(siteRoute.siteName)
        val siteClientDto = siteService.find(siteServerDto, sessionId)
        call.respond(siteClientDto)
    }

    override suspend fun delete(call: ApplicationCall, siteRoute: SiteRoute.Delete) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val siteServerDto = SiteServerDto(siteRoute.siteName)
        val siteClientDto = siteService.delete(siteServerDto, sessionId)
        call.respond(siteClientDto)
    }

}
