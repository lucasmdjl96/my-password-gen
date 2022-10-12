package com.lucasmdjl.passwordgenerator.server.service.impl

import com.lucasmdjl.passwordgenerator.common.dto.server.EmailServerDto
import com.lucasmdjl.passwordgenerator.server.repository.EmailRepository
import com.lucasmdjl.passwordgenerator.server.repository.UserRepository
import com.lucasmdjl.passwordgenerator.server.service.EmailService
import com.lucasmdjl.passwordgenerator.server.service.SessionService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

private val logger = KotlinLogging.logger("EmailServiceImpl")

class EmailServiceImpl(
    private val emailRepository: EmailRepository,
    private val userRepository: UserRepository,
    private val sessionService: SessionService
) : EmailService {

    override fun create(emailServerDto: EmailServerDto, sessionId: UUID) = transaction {
        logger.debug { "create" }
        val (emailAddress) = emailServerDto
        val user = sessionService.getLastUser(sessionId)!!
        val id = emailRepository.createAndGetId(emailAddress, user)
        val email = if (id != null) emailRepository.getById(id) else null
        userRepository.setLastEmail(user, email)
        email
    }

    override fun find(emailServerDto: EmailServerDto, sessionId: UUID) = transaction {
        logger.debug { "find" }
        val (emailAddress) = emailServerDto
        val user = sessionService.getLastUser(sessionId)!!
        val email = emailRepository.getByAddressAndUser(emailAddress, user)
        userRepository.setLastEmail(user, email)
        email
    }

    override fun delete(emailServerDto: EmailServerDto, sessionId: UUID) = transaction {
        logger.debug { "delete" }
        val email = find(emailServerDto, sessionId)
        if (email != null) emailRepository.delete(email)
        else null
    }
}
