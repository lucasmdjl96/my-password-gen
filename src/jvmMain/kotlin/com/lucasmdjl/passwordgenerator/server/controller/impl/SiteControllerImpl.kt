package com.lucasmdjl.passwordgenerator.server.controller.impl

import com.lucasmdjl.passwordgenerator.common.dto.server.SiteServerDto
import com.lucasmdjl.passwordgenerator.common.routes.SiteRoute
import com.lucasmdjl.passwordgenerator.server.controller.SiteController
import com.lucasmdjl.passwordgenerator.server.crypto.encode
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.mapper.SiteMapper
import com.lucasmdjl.passwordgenerator.server.plugins.NotAuthenticatedException
import com.lucasmdjl.passwordgenerator.server.service.SiteService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

class SiteControllerImpl(
    private val siteService: SiteService,
    private val siteMapper: SiteMapper
) : SiteController {

    override suspend fun post(call: ApplicationCall, siteRoute: SiteRoute.New) {
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val siteServerDto = call.receive<SiteServerDto>().encode()
        siteService.create(siteServerDto, sessionId)
        call.respond(HttpStatusCode.OK)
    }

    override suspend fun get(call: ApplicationCall, siteRoute: SiteRoute.Find) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val siteName = siteRoute.siteName.encode()
        val siteServerDto = SiteServerDto(siteName)
        siteService.find(siteServerDto, sessionId)
        call.respond(HttpStatusCode.OK)
    }

    override suspend fun delete(call: ApplicationCall, siteRoute: SiteRoute.Delete) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val siteName = siteRoute.siteName.encode()
        val siteServerDto = SiteServerDto(siteName)
        siteService.delete(siteServerDto, sessionId)
        call.respond(HttpStatusCode.OK)
    }

}
