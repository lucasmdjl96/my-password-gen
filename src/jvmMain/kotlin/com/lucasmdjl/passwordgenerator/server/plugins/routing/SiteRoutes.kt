package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.dto.server.SiteServerDto
import com.lucasmdjl.passwordgenerator.common.routes.SiteRoute
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.mapper.impl.SiteMapperImpl.toSiteClientDto
import com.lucasmdjl.passwordgenerator.server.siteService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger("SiteRoutes")

fun Route.siteRoutes() {
    post<SiteRoute.New> {
        logger.debug { call.request.path() }
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val siteServerDto = call.receive<SiteServerDto>()
        val siteClientDto = siteService.create(siteServerDto, sessionId)?.toSiteClientDto()
        call.respondNullable(siteClientDto)
    }
    get<SiteRoute.Find> { siteRoute ->
        logger.debug { call.request.path() }
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val username = siteRoute.username
        val emailAddress = siteRoute.emailAddress
        val siteName = siteRoute.siteName
        val siteServerDto = SiteServerDto(siteName, emailAddress, username)
        val siteClientDto = siteService.find(siteServerDto, sessionId)?.toSiteClientDto()
        call.respondNullable(siteClientDto)
    }
    delete<SiteRoute.Delete> { siteRoute ->
        logger.debug { call.request.path() }
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val username = siteRoute.username
        val emailAddress = siteRoute.emailAddress
        val siteName = siteRoute.siteName
        val siteServerDto = SiteServerDto(siteName, emailAddress, username)
        val result = siteService.delete(siteServerDto, sessionId)
        call.respondNullable(result)
    }
}
