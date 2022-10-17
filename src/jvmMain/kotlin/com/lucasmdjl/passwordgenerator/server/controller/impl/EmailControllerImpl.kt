package com.lucasmdjl.passwordgenerator.server.controller.impl

import com.lucasmdjl.passwordgenerator.common.dto.server.EmailServerDto
import com.lucasmdjl.passwordgenerator.common.routes.EmailRoute
import com.lucasmdjl.passwordgenerator.server.controller.EmailController
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.mapper.EmailMapper
import com.lucasmdjl.passwordgenerator.server.plugins.NotAuthenticatedException
import com.lucasmdjl.passwordgenerator.server.service.EmailService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

class EmailControllerImpl(
    private val emailService: EmailService,
    private val emailMapper: EmailMapper
) : EmailController {

    override suspend fun post(call: ApplicationCall, emailRoute: EmailRoute.New) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val emailServerDto = call.receive<EmailServerDto>()
        val emailClientDto = with(emailMapper) {
            emailService.create(emailServerDto, sessionId).toEmailClientDto()
        }
        call.respond(emailClientDto)
    }

    override suspend fun get(call: ApplicationCall, emailRoute: EmailRoute.Find) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val emailAddress = emailRoute.emailAddress
        val emailServerDto = EmailServerDto(emailAddress)
        val emailClientDto = with(emailMapper) {
            emailService.find(emailServerDto, sessionId).toEmailClientDto()
        }
        call.respond(emailClientDto)
    }

    override suspend fun delete(call: ApplicationCall, emailRoute: EmailRoute.Delete) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val emailAddress = emailRoute.emailAddress
        val emailServerDto = EmailServerDto(emailAddress)
        emailService.delete(emailServerDto, sessionId)
        call.respond(HttpStatusCode.OK)
    }

}
