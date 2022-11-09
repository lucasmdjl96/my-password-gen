package com.mypasswordgen.server.service.impl

import com.mypasswordgen.common.dto.server.UserServerDto
import com.mypasswordgen.server.mapper.UserMapper
import com.mypasswordgen.server.plugins.DataConflictException
import com.mypasswordgen.server.plugins.DataNotFoundException
import com.mypasswordgen.server.repository.UserRepository
import com.mypasswordgen.server.service.SessionService
import com.mypasswordgen.server.service.UserService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

private val logger = KotlinLogging.logger("UserServiceImpl")

class UserServiceImpl(
    private val userRepository: UserRepository,
    private val sessionService: SessionService,
    private val userMapper: UserMapper
) :
    UserService {

    override fun create(userServerDto: UserServerDto, sessionId: UUID) = transaction {
        logger.debug { "create" }
        val username = userServerDto.username
        if (userRepository.getByNameAndSession(username, sessionId) != null) throw DataConflictException()
        val id = userRepository.createAndGetId(username, sessionId)
        val user = userRepository.getById(id) ?: throw DataNotFoundException()
        sessionService.setLastUser(sessionId, user)
        with(userMapper) {
            user.toUserClientDto()
        }
    }

    override fun find(userServerDto: UserServerDto, sessionId: UUID) = transaction {
        logger.debug { "find" }
        val username = userServerDto.username
        val user = userRepository.getByNameAndSession(username, sessionId) ?: throw DataNotFoundException()
        sessionService.setLastUser(sessionId, user)
        with(userMapper) {
            user.toUserClientDto()
        }
    }

    override fun logout(userServerDto: UserServerDto, sessionId: UUID): Unit = transaction {
        logger.debug { "logout" }
        val user = sessionService.getLastUser(sessionId) ?: throw DataNotFoundException()
        if (user.username != userServerDto.username) throw DataNotFoundException()
        userRepository.setLastEmail(user, null)
        sessionService.setLastUser(sessionId, null)
    }

}
