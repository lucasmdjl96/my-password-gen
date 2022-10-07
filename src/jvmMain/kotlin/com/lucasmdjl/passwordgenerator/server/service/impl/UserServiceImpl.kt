package com.lucasmdjl.passwordgenerator.server.service.impl

import com.lucasmdjl.passwordgenerator.common.dto.server.UserServerDto
import com.lucasmdjl.passwordgenerator.server.model.Session
import com.lucasmdjl.passwordgenerator.server.repository.UserRepository
import com.lucasmdjl.passwordgenerator.server.repository.impl.UserRepositoryImpl
import com.lucasmdjl.passwordgenerator.server.service.UserService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

private val logger = KotlinLogging.logger("UserServiceImpl")

object UserServiceImpl : UserService {

    private val userRepository: UserRepository = UserRepositoryImpl

    override fun create(userServerDto: UserServerDto, sessionId: UUID) = transaction {
        logger.debug { "create" }
        val username = userServerDto.username
        val id = userRepository.createAndGetId(username, sessionId)
        if (id != null) userRepository.getById(id) else null
    }

    override fun find(userServerDto: UserServerDto, sessionId: UUID) = transaction {
        logger.debug { "find" }
        val username = userServerDto.username
        userRepository.getByNameAndSession(username, sessionId)
    }


    override fun moveAllUsers(fromSession: Session, toSession: Session) = transaction {
        logger.debug { "moveAllUsers" }
        userRepository.moveAll(fromSession.id.value, toSession.id.value)
    }

}
