package com.lucasmdjl.passwordgenerator.server.service.impl

import com.lucasmdjl.passwordgenerator.common.dto.server.EmailServerDto
import com.lucasmdjl.passwordgenerator.server.plugins.DataConflictException
import com.lucasmdjl.passwordgenerator.server.plugins.DataNotFoundException
import com.lucasmdjl.passwordgenerator.server.plugins.NotEnoughInformationException
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
        val user = sessionService.getLastUser(sessionId) ?: throw NotEnoughInformationException()
        if (emailRepository.getByAddressAndUser(emailAddress, user) != null) throw DataConflictException()
        val id = emailRepository.createAndGetId(emailAddress, user)
        val email = emailRepository.getById(id) ?: throw DataNotFoundException()
        userRepository.setLastEmail(user, email)
        email
    }

    override fun find(emailServerDto: EmailServerDto, sessionId: UUID) = transaction {
        logger.debug { "find" }
        val (emailAddress) = emailServerDto
        val user = sessionService.getLastUser(sessionId) ?: throw NotEnoughInformationException()
        val email = emailRepository.getByAddressAndUser(emailAddress, user) ?: throw DataNotFoundException()
        userRepository.setLastEmail(user, email)
        email
    }

    override fun delete(emailServerDto: EmailServerDto, sessionId: UUID) = transaction {
        logger.debug { "delete" }
        val email = find(emailServerDto, sessionId)
        emailRepository.delete(email)
    }
}
