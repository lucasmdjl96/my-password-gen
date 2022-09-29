package com.lucasmdjl.application.plugins.routing

import com.lucasmdjl.application.dto.SessionDto
import com.lucasmdjl.application.emailService
import com.lucasmdjl.application.siteMapper
import com.lucasmdjl.application.siteService
import com.lucasmdjl.application.userService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*

fun Route.siteRoutes() {
    route("/site") {
        post("/new") {
            val sessionId = call.sessions.get<SessionDto>()!!.sessionId
            val username = call.request.queryParameters.getOrFail("username")
            val user = userService.getByName(username, sessionId)!!
            val emailAddress = call.request.queryParameters.getOrFail("emailAddress")
            val email = emailService.getEmailFromUser(emailAddress, user)!!
            val siteName = call.receiveText().trim('"')
            val site = siteService.addSiteToEmail(siteName, email)
            val siteDto = if (site != null) {
                siteMapper.siteToSiteDto(site)
            } else {
                null
            }
            call.respondNullable(siteDto)
        }
        get("/find/{siteName}") {
            val sessionId = call.sessions.get<SessionDto>()!!.sessionId
            val username = call.request.queryParameters.getOrFail("username")
            val user = userService.getByName(username, sessionId)!!
            val emailAddress = call.request.queryParameters.getOrFail("emailAddress")
            val email = emailService.getEmailFromUser(emailAddress, user)!!
            val siteName = call.parameters.getOrFail("siteName")
            val site = siteService.getSiteFromEmail(siteName, email)
            val siteDto = if (site != null) {
                siteMapper.siteToSiteDto(site)
            } else {
                null
            }
            call.respondNullable(siteDto)
        }
        delete("/delete/{siteName}") {
            val sessionId = call.sessions.get<SessionDto>()!!.sessionId
            val username = call.request.queryParameters.getOrFail("username")
            val user = userService.getByName(username, sessionId)!!
            val emailAddress = call.request.queryParameters.getOrFail("emailAddress")
            val email = emailService.getEmailFromUser(emailAddress, user)!!
            val siteName = call.parameters.getOrFail("siteName")
            val result = siteService.removeSiteFromEmail(siteName, email)
            call.respondNullable(result)
        }
    }
}