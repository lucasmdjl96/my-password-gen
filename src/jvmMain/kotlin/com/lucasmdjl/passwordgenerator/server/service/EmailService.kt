package com.lucasmdjl.passwordgenerator.server.service

import com.lucasmdjl.passwordgenerator.common.dto.server.EmailServerDto
import com.lucasmdjl.passwordgenerator.server.model.Email
import java.util.*

interface EmailService {

    fun create(emailServerDto: EmailServerDto, sessionId: UUID): Email?

    fun find(emailServerDto: EmailServerDto, sessionId: UUID): Email?

    fun delete(emailServerDto: EmailServerDto, sessionId: UUID): Unit?

}
