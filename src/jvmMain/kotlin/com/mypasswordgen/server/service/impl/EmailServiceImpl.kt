package com.mypasswordgen.server.service.impl

import com.mypasswordgen.common.dto.server.EmailServerDto
import com.mypasswordgen.server.mapper.EmailMapper
import com.mypasswordgen.server.plugins.DataConflictException
import com.mypasswordgen.server.plugins.DataNotFoundException
import com.mypasswordgen.server.plugins.NotEnoughInformationException
import com.mypasswordgen.server.repository.EmailRepository
import com.mypasswordgen.server.repository.UserRepository
import com.mypasswordgen.server.service.EmailService
import com.mypasswordgen.server.service.SessionService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

private val logger = KotlinLogging.logger("EmailServiceImpl")

class EmailServiceImpl(
    private val emailRepository: EmailRepository,
    private val userRepository: UserRepository,
    private val sessionService: SessionService,
    private val emailMapper: EmailMapper
) : EmailService {

    override fun create(emailServerDto: EmailServerDto, sessionId: UUID) = transaction {
        logger.debug { "create" }
        val (emailAddress) = emailServerDto
        val user = sessionService.getLastUser(sessionId) ?: throw NotEnoughInformationException()
        if (emailRepository.getByAddressAndUser(emailAddress, user) != null) throw DataConflictException()
        val id = emailRepository.createAndGetId(emailAddress, user)
        val email = emailRepository.getById(id) ?: throw DataNotFoundException()
        userRepository.setLastEmail(user, email)
        with(emailMapper) {
            email.toEmailClientDto()
        }
    }

    override fun find(emailServerDto: EmailServerDto, sessionId: UUID) = transaction {
        logger.debug { "find" }
        val (emailAddress) = emailServerDto
        val user = sessionService.getLastUser(sessionId) ?: throw NotEnoughInformationException()
        val email = emailRepository.getByAddressAndUser(emailAddress, user) ?: throw DataNotFoundException()
        userRepository.setLastEmail(user, email)
        with(emailMapper) {
            email.toEmailClientDto()
        }
    }

    override fun delete(emailServerDto: EmailServerDto, sessionId: UUID) = transaction {
        logger.debug { "delete" }
        val (emailAddress) = emailServerDto
        val user = sessionService.getLastUser(sessionId) ?: throw NotEnoughInformationException()
        val email = emailRepository.getByAddressAndUser(emailAddress, user) ?: throw DataNotFoundException()
        val emailClientDto = with(emailMapper) {
            email.toEmailClientDto()
        }
        emailRepository.delete(email)
        emailClientDto
    }
}