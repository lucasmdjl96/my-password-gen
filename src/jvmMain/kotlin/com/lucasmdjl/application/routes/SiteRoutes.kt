package com.lucasmdjl.application.routes

import com.lucasmdjl.application.siteService
import dto.SessionCookie
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import java.util.*

fun Routing.siteRoutes() {
    route("/site") {
        post("/new") {
            val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
            val emailDto = siteService.addSiteToEmail(
                call.receiveText().trim('"'),
                call.request.queryParameters.getOrFail("emailAddress"),
                call.request.queryParameters.getOrFail("username"),
                sessionId
            )
            call.respond(emailDto)
        }
        get("/find/{siteName}") {
            val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
            val siteDto = siteService.getSiteFromEmail(
                call.parameters.getOrFail("siteName"),
                call.request.queryParameters.getOrFail("emailAddress"),
                call.request.queryParameters.getOrFail("username"),
                sessionId
            )
            call.respondNullable(siteDto)
        }
    }
}