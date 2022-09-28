package com.lucasmdjl.application.plugins.routing

import com.lucasmdjl.application.*
import com.lucasmdjl.application.dto.SessionCookie
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import java.util.*

fun Route.siteRoutes() {
    route("/site") {
        post("/new") {
            val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
            val username = call.request.queryParameters.getOrFail("username")
            val user = userService.getByName(username, sessionId)!!
            val emailAddress = call.request.queryParameters.getOrFail("emailAddress")
            val email = emailService.getEmailFromUser(emailAddress, user)!!
            val siteName = call.receiveText().trim('"')
            siteService.addSiteToEmail(siteName, email)
            val emailDto = emailMapper.emailToEmailDto(email)
            call.respond(emailDto)
        }
        get("/find/{siteName}") {
            val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
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
            val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
            val username = call.request.queryParameters.getOrFail("username")
            val user = userService.getByName(username, sessionId)!!
            val emailAddress = call.request.queryParameters.getOrFail("emailAddress")
            val email = emailService.getEmailFromUser(emailAddress, user)!!
            val siteName = call.parameters.getOrFail("siteName")
            siteService.removeSiteFromEmail(siteName, email)
            val emailDto = emailMapper.emailToEmailDto(email)
            call.respond(emailDto)
        }
    }
}