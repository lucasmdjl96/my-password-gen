package com.lucasmdjl.passwordgenerator.server.controller.impl

import com.lucasmdjl.passwordgenerator.common.dto.server.EmailServerDto
import com.lucasmdjl.passwordgenerator.common.routes.EmailRoute
import com.lucasmdjl.passwordgenerator.server.controller.EmailController
import com.lucasmdjl.passwordgenerator.server.crypto.encode
import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.plugins.NotAuthenticatedException
import com.lucasmdjl.passwordgenerator.server.service.EmailService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

class EmailControllerImpl(
    private val emailService: EmailService
) : EmailController {

    override suspend fun post(call: ApplicationCall, emailRoute: EmailRoute.New) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val emailServerDto = call.receive<EmailServerDto>().encode()
        val emailClientDto = emailService.create(emailServerDto, sessionId)
        call.respond(emailClientDto)
    }

    override suspend fun get(call: ApplicationCall, emailRoute: EmailRoute.Find) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val emailServerDto = EmailServerDto(emailRoute.emailAddress).encode()
        val emailClientDto = emailService.find(emailServerDto, sessionId)
        call.respond(emailClientDto)
    }

    override suspend fun delete(call: ApplicationCall, emailRoute: EmailRoute.Delete) {
        val sessionId = call.sessions.get<SessionDto>()?.sessionId ?: throw NotAuthenticatedException()
        val emailServerDto = EmailServerDto(emailRoute.emailAddress).encode()
        val emailClientDto = emailService.delete(emailServerDto, sessionId)
        call.respond(emailClientDto)
    }

}
