package com.lucasmdjl.application.plugins.routing

import com.lucasmdjl.application.dto.SessionDto
import com.lucasmdjl.application.emailService
import com.lucasmdjl.application.siteMapper
import com.lucasmdjl.application.siteService
import com.lucasmdjl.application.userService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import mu.KotlinLogging
import routes.SiteRoute

private val logger = KotlinLogging.logger("SiteRoutes")

fun Route.siteRoutes() {
    post<SiteRoute.New> { siteRoute ->
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val username = siteRoute.username
        val emailAddress = siteRoute.emailAddress
        val siteName = call.receiveText().trim('"')
        logger.debug {
            "/site/new: call with sessionId: $sessionId, username: $username, " +
                    "emailAddress: $emailAddress, siteName: $siteName"
        }
        val user = userService.getByName(username, sessionId)!!
        val email = emailService.getEmailFromUser(emailAddress, user)!!
        val site = siteService.addSiteToEmail(siteName, email)
        val siteDto = if (site != null) {
            siteMapper.siteToSiteDto(site)
        } else {
            null
        }
        logger.debug { "/site/new: respond with siteDto: $siteDto" }
        call.respondNullable(siteDto)
    }
    get<SiteRoute.Find> { siteRoute ->
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val username = siteRoute.username
        val emailAddress = siteRoute.emailAddress
        val siteName = siteRoute.siteName
        logger.debug {
            "/site/find: call with sessionId: $sessionId, username: $username, " +
                    "emailAddress: $emailAddress, siteName: $siteName"
        }
        val user = userService.getByName(username, sessionId)!!
        val email = emailService.getEmailFromUser(emailAddress, user)!!
        val site = siteService.getSiteFromEmail(siteName, email)
        val siteDto = if (site != null) {
            siteMapper.siteToSiteDto(site)
        } else {
            null
        }
        logger.debug { "/site/find: respond with siteDto: $siteDto" }
        call.respondNullable(siteDto)
    }
    delete<SiteRoute.Delete> { siteRoute ->
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val username = siteRoute.username
        val emailAddress = siteRoute.emailAddress
        val siteName = siteRoute.siteName
        logger.debug {
            "/site/delete: call with sessionId: $sessionId, username: $username, " +
                    "emailAddress: $emailAddress, siteName: $siteName"
        }
        val user = userService.getByName(username, sessionId)!!
        val email = emailService.getEmailFromUser(emailAddress, user)!!
        val result = siteService.removeSiteFromEmail(siteName, email)
        logger.debug { "/site/delete: respond with result: $result" }
        call.respondNullable(result)
    }
}