package com.lucasmdjl.passwordgenerator.server.controller.impl

import com.lucasmdjl.passwordgenerator.common.dto.server.EmailServerDto
import com.lucasmdjl.passwordgenerator.common.routes.EmailRoute
import com.lucasmdjl.passwordgenerator.server.controller.EmailController
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.mapper.EmailMapper
import com.lucasmdjl.passwordgenerator.server.service.EmailService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

class EmailControllerImpl(
    private val emailService: EmailService,
    private val emailMapper: EmailMapper
) : EmailController {

    override suspend fun post(call: ApplicationCall, emailRoute: EmailRoute.New) {
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val emailServerDto = call.receive<EmailServerDto>()
        val emailClientDto = with(emailMapper) {
            emailService.create(emailServerDto, sessionId)?.toEmailClientDto()
        }
        call.respondNullable(emailClientDto)
    }

    override suspend fun get(call: ApplicationCall, emailRoute: EmailRoute.Find) {
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val emailAddress = emailRoute.emailAddress
        val emailServerDto = EmailServerDto(emailAddress)
        val emailClientDto = with(emailMapper) {
            emailService.find(emailServerDto, sessionId)?.toEmailClientDto()
        }
        call.respondNullable(emailClientDto)
    }

    override suspend fun delete(call: ApplicationCall, emailRoute: EmailRoute.Delete) {
        val sessionId = call.sessions.get<SessionDto>()!!.sessionId
        val emailAddress = emailRoute.emailAddress
        val emailServerDto = EmailServerDto(emailAddress)
        val result = emailService.delete(emailServerDto, sessionId)
        call.respondNullable(result)
    }

}
