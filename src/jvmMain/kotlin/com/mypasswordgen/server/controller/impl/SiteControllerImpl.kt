package com.mypasswordgen.server.controller.impl

import com.mypasswordgen.common.dto.server.SiteServerDto
import com.mypasswordgen.common.routes.SiteRoute
import com.mypasswordgen.server.controller.SiteController
import com.mypasswordgen.server.crypto.encode
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
        val siteServerDto = call.receive<SiteServerDto>().encode()
        val siteClientDto = siteService.create(siteServerDto, sessionId)
        call.respond(siteClientDto)
    }

    override suspend fun get(call: ApplicationCall, siteRoute: SiteRoute.Find) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val siteServerDto = SiteServerDto(siteRoute.siteName).encode()
        val siteClientDto = siteService.find(siteServerDto, sessionId)
        call.respond(siteClientDto)
    }

    override suspend fun delete(call: ApplicationCall, siteRoute: SiteRoute.Delete) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val siteServerDto = SiteServerDto(siteRoute.siteName).encode()
        val siteClientDto = siteService.delete(siteServerDto, sessionId)
        call.respond(siteClientDto)
    }

}
