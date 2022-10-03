package com.lucasmdjl.passwordgenerator.server.service.impl

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

    override fun create(username: String, sessionId: UUID) = transaction {
        logger.debug { "create call with username: $username, sessionId: $sessionId" }
        val id = userRepository.createAndGetId(username, sessionId)
        if (id != null) userRepository.getById(id) else null
    }

    override fun getByName(username: String, sessionId: UUID) = transaction {
        logger.debug { "getByName call with username: $username, sessionId: $sessionId" }
        userRepository.getByNameAndSession(username, sessionId)
    }


    override fun moveAllUsers(fromSession: Session, toSession: Session) = transaction {
        logger.debug { "moveAllUsers call with fromSession: $fromSession, toSession: $toSession" }
        userRepository.moveAll(fromSession, toSession)
    }

}