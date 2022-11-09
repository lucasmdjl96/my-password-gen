package com.mypasswordgen.server.service.impl

import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.model.Session
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.repository.SessionRepository
import com.mypasswordgen.server.repository.UserRepository
import com.mypasswordgen.server.service.SessionService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

private val logger = KotlinLogging.logger("SessionServiceImpl")

class SessionServiceImpl(
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository
) : SessionService {

    override fun assignNew(oldSessionDto: SessionDto?) = transaction {
        logger.debug { "assignNew" }
        val newSession = sessionRepository.create()
        if (oldSessionDto != null) {
            val oldSession = find(oldSessionDto)
            if (oldSession != null) {
                val lastUser = sessionRepository.getLastUser(oldSession)
                if (lastUser != null) userRepository.setLastEmail(lastUser, null)
                moveAllUsers(oldSession, newSession)
                sessionRepository.delete(oldSession)
            }
        }
        newSession
    }

    override fun find(sessionDto: SessionDto) = transaction {
        logger.debug { "find" }
        sessionRepository.getById(sessionDto.sessionId)
    }

    override fun delete(sessionDto: SessionDto): Unit? = transaction {
        logger.debug { "delete" }
        val session = find(sessionDto)
        if (session != null) sessionRepository.delete(session) else null
    }

    override fun setLastUser(sessionId: UUID, user: User?): Unit = transaction {
        logger.debug { "setLastUser" }
        val session = sessionRepository.getById(sessionId)!!
        sessionRepository.setLastUser(session, user)
    }

    override fun getLastUser(sessionId: UUID): User? = transaction {
        logger.debug { "getLastUser" }
        val session = sessionRepository.getById(sessionId)!!
        sessionRepository.getLastUser(session)
    }

    override fun moveAllUsers(fromSession: Session, toSession: Session) = transaction {
        logger.debug { "moveAllUsers" }
        userRepository.moveAll(fromSession.id.value, toSession.id.value)
    }


}
