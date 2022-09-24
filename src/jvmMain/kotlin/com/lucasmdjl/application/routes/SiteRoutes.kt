package com.lucasmdjl.application.routes

import SessionCookie
import com.lucasmdjl.application.siteService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import java.util.*

fun Routing.siteRoutes() {
    post("/new/site/{username}/{emailAddress}") {
        val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
        val emailDto = siteService.addSiteToEmail(
            call.receiveText().trim('"'),
            call.parameters.getOrFail("emailAddress"),
            call.parameters.getOrFail("username"),
            sessionId
        )
        call.respond(emailDto)
    }
    post("/find/site/{username}/{emailAddress}") {
        val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
        val siteDto = siteService.getSiteFromEmail(
            call.receiveText().trim('"'),
            call.parameters.getOrFail("emailAddress"),
            call.parameters.getOrFail("username"),
            sessionId
        )
        call.respondNullable(siteDto)
    }
}