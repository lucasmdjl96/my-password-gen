package com.lucasmdjl.passwordgenerator.server.plugins.routing

import com.lucasmdjl.passwordgenerator.common.dto.server.EmailServerDto
import com.lucasmdjl.passwordgenerator.common.routes.EmailRoute
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.emailService
import com.lucasmdjl.passwordgenerator.server.mapper.impl.EmailMapperImpl.toEmailClientDto
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
    post<EmailRoute.New> {
        logger.debug { call.request.path() }
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val emailServerDto = call.receive<EmailServerDto>()
        val emailClientDto = emailService.create(emailServerDto, sessionId)?.toEmailClientDto()
        call.respondNullable(emailClientDto)
    }
    get<EmailRoute.Find> { emailRoute ->
        logger.debug { call.request.path() }
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val username = emailRoute.username
        val emailAddress = emailRoute.emailAddress
        val emailServerDto = EmailServerDto(emailAddress, username)
        val emailClientDto = emailService.find(emailServerDto, sessionId)?.toEmailClientDto()
        logger.debug { "/email/find: respond with emailDto: $emailClientDto" }
        call.respondNullable(emailClientDto)
    }
    delete<EmailRoute.Delete> { emailRoute ->
        logger.debug { call.request.path() }
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val username = emailRoute.username
        val emailAddress = emailRoute.emailAddress
        val emailServerDto = EmailServerDto(emailAddress, username)
        val result = emailService.delete(emailServerDto, sessionId)
        logger.debug { "/email/delete: respond with result: $result" }
        call.respondNullable(result)
    }
}
