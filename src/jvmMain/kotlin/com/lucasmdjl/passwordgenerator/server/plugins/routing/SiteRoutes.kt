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
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val siteServerDto = call.receive<SiteServerDto>()
        val siteClientDto = siteService.create(siteServerDto, sessionId)?.toSiteClientDto()
        logger.debug { "/site/new: respond with siteDto: $siteClientDto" }
        call.respondNullable(siteClientDto)
    }
    get<SiteRoute.Find> { siteRoute ->
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val username = siteRoute.username
        val emailAddress = siteRoute.emailAddress
        val siteName = siteRoute.siteName
        val siteServerDto = SiteServerDto(siteName, emailAddress, username)
        logger.debug {
            "/site/find: call with sessionId: $sessionId, username: $username, " +
                    "emailAddress: $emailAddress, siteName: $siteName"
        }
        val siteClientDto = siteService.find(siteServerDto, sessionId)?.toSiteClientDto()
        logger.debug { "/site/find: respond with siteDto: $siteClientDto" }
        call.respondNullable(siteClientDto)
    }
    delete<SiteRoute.Delete> { siteRoute ->
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val username = siteRoute.username
        val emailAddress = siteRoute.emailAddress
        val siteName = siteRoute.siteName
        val siteServerDto = SiteServerDto(siteName, emailAddress, username)
        logger.debug {
            "/site/delete: call with sessionId: $sessionId, username: $username, " +
                    "emailAddress: $emailAddress, siteName: $siteName"
        }
        val result = siteService.delete(siteServerDto, sessionId)
        logger.debug { "/site/delete: respond with result: $result" }
        call.respondNullable(result)
    }
}
