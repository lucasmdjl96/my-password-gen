package com.lucasmdjl.application.plugins.routing

import com.lucasmdjl.application.dto.SessionDto
import com.lucasmdjl.application.emailMapper
import com.lucasmdjl.application.emailService
import com.lucasmdjl.application.userService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger("EmailRoutes")

fun Route.emailRoutes() {
    route("/email") {
        post("/new") {
            val sessionId = call.sessions.get<SessionDto>()!!.sessionId
            val username = call.request.queryParameters.getOrFail("username")
            val emailAddress = call.receiveText().trim('"')
            logger.debug {
                "/email/new: call with sessionId: $sessionId, username: $username, emailAddress: $emailAddress"
            }
            val user = userService.getByName(username, sessionId)!!
            val email = emailService.addEmailToUser(emailAddress, user)
            val emailDto = if (email != null) {
                emailMapper.emailToEmailDto(email)
            } else {
                null
            }
            logger.debug { "/email/new: respond with emailDto: $emailDto" }
            call.respondNullable(emailDto)
        }
        get("/find/{emailAddress}") {
            val sessionId = call.sessions.get<SessionDto>()!!.sessionId
            val username = call.request.queryParameters.getOrFail("username")
            val emailAddress = call.parameters.getOrFail("emailAddress")
            logger.debug {
                "/email/find: call with sessionId: $sessionId, username: $username, emailAddress: $emailAddress"
            }
            val user = userService.getByName(username, sessionId)!!
            val email = emailService.getEmailFromUser(emailAddress, user)
            val emailDto = if (email != null) {
                emailMapper.emailToEmailDto(email)
            } else {
                null
            }
            logger.debug { "/email/find: respond with emailDto: $emailDto" }
            call.respondNullable(emailDto)
        }
        delete("/delete/{emailAddress}") {
            val sessionId = call.sessions.get<SessionDto>()!!.sessionId
            val username = call.request.queryParameters.getOrFail("username")
            val emailAddress = call.parameters.getOrFail("emailAddress")
            logger.debug {
                "/email/delete: call with sessionId: $sessionId, username: $username, emailAddress: $emailAddress"
            }
            val user = userService.getByName(username, sessionId)!!
            val result = emailService.removeEmailFromUser(emailAddress, user)
            logger.debug { "/email/delete: respond with result: $result" }
            call.respondNullable(result)
        }
    }
}