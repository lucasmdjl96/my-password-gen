package com.lucasmdjl.passwordgenerator.server.service.impl

import com.lucasmdjl.passwordgenerator.common.dto.server.EmailServerDto
import com.lucasmdjl.passwordgenerator.server.repository.EmailRepository
import com.lucasmdjl.passwordgenerator.server.repository.impl.EmailRepositoryImpl
import com.lucasmdjl.passwordgenerator.server.service.EmailService
import com.lucasmdjl.passwordgenerator.server.userService
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object EmailServiceImpl : EmailService {

    private val emailRepository: EmailRepository = EmailRepositoryImpl

    override fun create(emailServerDto: EmailServerDto, sessionId: UUID) = transaction {
        val (emailAddress, userServerDto) = emailServerDto
        val user = userService.find(userServerDto, sessionId)!!
        val id = emailRepository.createAndGetId(emailAddress, user)
        if (id != null) emailRepository.getById(id) else null
    }

    override fun find(emailServerDto: EmailServerDto, sessionId: UUID) = transaction {
        val (emailAddress, userServerDto) = emailServerDto
        val user = userService.find(userServerDto, sessionId)!!
        emailRepository.getByAddressAndUser(emailAddress, user)
    }

    override fun delete(emailServerDto: EmailServerDto, sessionId: UUID) = transaction {
        val email = find(emailServerDto, sessionId)
        if (email != null) emailRepository.delete(email)
        else null
    }
}
