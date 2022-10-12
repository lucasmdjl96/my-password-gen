package com.lucasmdjl.passwordgenerator.server.controller.impl

import com.lucasmdjl.passwordgenerator.common.dto.server.SiteServerDto
import com.lucasmdjl.passwordgenerator.common.routes.SiteRoute
import com.lucasmdjl.passwordgenerator.server.controller.SiteController
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.mapper.SiteMapper
import com.lucasmdjl.passwordgenerator.server.service.SiteService
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
        val siteServerDto = call.receive<SiteServerDto>()
        val siteClientDto = with(siteMapper) {
            siteService.create(siteServerDto, sessionId)?.toSiteClientDto()
        }
        call.respondNullable(siteClientDto)
    }

    override suspend fun get(call: ApplicationCall, siteRoute: SiteRoute.Find) {
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val siteName = siteRoute.siteName
        val siteServerDto = SiteServerDto(siteName)
        val siteClientDto = with(siteMapper) {
            siteService.find(siteServerDto, sessionId)?.toSiteClientDto()
        }
        call.respondNullable(siteClientDto)
    }

    override suspend fun delete(call: ApplicationCall, siteRoute: SiteRoute.Delete) {
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val siteName = siteRoute.siteName
        val siteServerDto = SiteServerDto(siteName)
        val result = siteService.delete(siteServerDto, sessionId)
        call.respondNullable(result)
    }

}
