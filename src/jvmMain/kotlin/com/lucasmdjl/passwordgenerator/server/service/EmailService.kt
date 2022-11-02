package com.lucasmdjl.passwordgenerator.server.service

import com.lucasmdjl.passwordgenerator.common.dto.client.EmailClientDto
import com.lucasmdjl.passwordgenerator.common.dto.server.EmailServerDto
import java.util.*

interface EmailService {

    fun create(emailServerDto: EmailServerDto, sessionId: UUID): EmailClientDto

    fun find(emailServerDto: EmailServerDto, sessionId: UUID): EmailClientDto

    fun delete(emailServerDto: EmailServerDto, sessionId: UUID): EmailClientDto

}
