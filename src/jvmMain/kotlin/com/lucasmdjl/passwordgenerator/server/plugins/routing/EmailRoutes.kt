package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.routes.EmailRoute
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.emailMapper
import com.lucasmdjl.passwordgenerator.server.emailService
import com.lucasmdjl.passwordgenerator.server.userService
import io.ktor.server.application.*

import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger("EmailRoutes")

fun Route.emailRoutes() {
    post<EmailRoute.New> { emailRoute ->
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val username = emailRoute.username
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
    get<EmailRoute.Find> { emailRoute ->
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val username = emailRoute.username
        val emailAddress = emailRoute.emailAddress
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
    delete<EmailRoute.Delete> { emailRoute ->
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val username = emailRoute.username
        val emailAddress = emailRoute.emailAddress
        logger.debug {
            "/email/delete: call with sessionId: $sessionId, username: $username, emailAddress: $emailAddress"
        }
        val user = userService.getByName(username, sessionId)!!
        val result = emailService.removeEmailFromUser(emailAddress, user)
        logger.debug { "/email/delete: respond with result: $result" }
        call.respondNullable(result)
    }
}