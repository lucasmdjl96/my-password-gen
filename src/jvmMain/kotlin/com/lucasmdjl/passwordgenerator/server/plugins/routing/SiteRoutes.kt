package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.dto.server.SiteServerDto
import com.lucasmdjl.passwordgenerator.common.routes.SiteRoute
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.emailService
import com.lucasmdjl.passwordgenerator.server.siteMapper
import com.lucasmdjl.passwordgenerator.server.siteService
import com.lucasmdjl.passwordgenerator.server.userService
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
        val username = siteServerDto.username
        val emailAddress = siteServerDto.emailAddress
        val siteName = siteServerDto.siteName
        val user = userService.getByName(username, sessionId)!!
        val email = emailService.getEmailFromUser(emailAddress, user)!!
        val site = siteService.addSiteToEmail(siteName, email)
        val siteClientDto = if (site != null) {
            siteMapper.siteToSiteClientDto(site)
        } else {
            null
        }
        call.respondNullable(siteClientDto)
    }
    get<SiteRoute.Find> { siteRoute ->
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val username = siteRoute.username
        val emailAddress = siteRoute.emailAddress
        val siteName = siteRoute.siteName
        val user = userService.getByName(username, sessionId)!!
        val email = emailService.getEmailFromUser(emailAddress, user)!!
        val site = siteService.getSiteFromEmail(siteName, email)
        val siteClientDto = if (site != null) {
            siteMapper.siteToSiteClientDto(site)
        } else {
            null
        }
        call.respondNullable(siteClientDto)
    }
    delete<SiteRoute.Delete> { siteRoute ->
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val username = siteRoute.username
        val emailAddress = siteRoute.emailAddress
        val siteName = siteRoute.siteName
        val user = userService.getByName(username, sessionId)!!
        val email = emailService.getEmailFromUser(emailAddress, user)!!
        val result = siteService.removeSiteFromEmail(siteName, email)
        call.respondNullable(result)
    }
}
