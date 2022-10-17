package com.lucasmdjl.passwordgenerator.server.service.impl

import com.lucasmdjl.passwordgenerator.common.dto.server.UserServerDto
import com.lucasmdjl.passwordgenerator.server.plugins.DataConflictException
import com.lucasmdjl.passwordgenerator.server.plugins.DataNotFoundException
import com.lucasmdjl.passwordgenerator.server.repository.UserRepository
import com.lucasmdjl.passwordgenerator.server.service.SessionService
import com.lucasmdjl.passwordgenerator.server.service.UserService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

private val logger = KotlinLogging.logger("UserServiceImpl")

class UserServiceImpl(private val userRepository: UserRepository, private val sessionService: SessionService) :
    UserService {

    override fun create(userServerDto: UserServerDto, sessionId: UUID) = transaction {
        logger.debug { "create" }
        val username = userServerDto.username
        val id = userRepository.createAndGetId(username, sessionId) ?: throw DataConflictException()
        val user = userRepository.getById(id) ?: throw DataNotFoundException()
        sessionService.setLastUser(sessionId, user)
        user
    }

    override fun find(userServerDto: UserServerDto, sessionId: UUID) = transaction {
        logger.debug { "find" }
        val username = userServerDto.username
        val user = userRepository.getByNameAndSession(username, sessionId) ?: throw DataNotFoundException()
        sessionService.setLastUser(sessionId, user)
        user
    }

    override fun logout(userServerDto: UserServerDto, sessionId: UUID): Unit = transaction {
        logger.debug { "logout" }
        val user = find(userServerDto, sessionId)
        userRepository.setLastEmail(user, null)
        sessionService.setLastUser(sessionId, null)

    }

}
