package com.mypasswordgen.server.service.impl

import com.mypasswordgen.common.dto.fullServer.FullUserServerDto
import com.mypasswordgen.common.dto.idb.UserIDBDto
import com.mypasswordgen.common.dto.server.UserServerDto
import com.mypasswordgen.server.mapper.UserMapper
import com.mypasswordgen.server.plugins.DataConflictException
import com.mypasswordgen.server.plugins.DataNotFoundException
import com.mypasswordgen.server.repository.SessionRepository
import com.mypasswordgen.server.repository.UserRepository
import com.mypasswordgen.server.service.EmailService
import com.mypasswordgen.server.service.UserService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

private val logger = KotlinLogging.logger("UserServiceImpl")

class UserServiceImpl(
    private val emailService: EmailService,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val userMapper: UserMapper
) :
    UserService {

    override fun create(userServerDto: UserServerDto, sessionId: UUID) = transaction {
        logger.debug { "create" }
        val username = userServerDto.username
        if (userRepository.getByNameAndSession(username, sessionId) != null) throw DataConflictException()
        val id = userRepository.createAndGetId(username, sessionId)
        val user = userRepository.getById(id) ?: throw DataNotFoundException()
        sessionRepository.setLastUser(sessionId, user)
        with(userMapper) {
            user.toUserClientDto()
        }
    }

    override fun find(userServerDto: UserServerDto, sessionId: UUID) = transaction {
        logger.debug { "find" }
        val username = userServerDto.username
        val user = userRepository.getByNameAndSession(username, sessionId) ?: throw DataNotFoundException()
        sessionRepository.setLastUser(sessionId, user)
        with(userMapper) {
            user.toUserClientDto()
        }
    }

    override fun logout(userServerDto: UserServerDto, sessionId: UUID): Unit = transaction {
        logger.debug { "logout" }
        val user = sessionRepository.getIfLastUser(sessionId, userServerDto.username) ?: throw DataNotFoundException()
        userRepository.setLastEmail(user, null)
        sessionRepository.setLastUser(sessionId, null)
    }

    override fun createFullUser(fullUser: FullUserServerDto, sessionId: UUID) = transaction {
        logger.debug { "createFullUser" }
        val username = fullUser.username
        if (userRepository.getByNameAndSession(username, sessionId) != null) throw DataConflictException()
        val id = userRepository.createAndGetId(username, sessionId)
        UserIDBDto {
            for (fullEmail in fullUser.emails) {
                +emailService.createFullEmail(fullEmail, id)
            }
        }

    }

    override fun getFullUser(userServerDto: UserServerDto, sessionId: UUID) = transaction {
        logger.debug { "getFullUser" }
        val username = userServerDto.username
        val user = userRepository.getByNameAndSession(username, sessionId) ?: throw DataNotFoundException()
        with(userMapper) {
            user.toFullUserClientDto()
        }
    }

}
