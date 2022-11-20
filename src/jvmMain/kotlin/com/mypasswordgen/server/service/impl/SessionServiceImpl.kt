package com.mypasswordgen.server.service.impl

import com.mypasswordgen.common.dto.FullSessionClientDto
import com.mypasswordgen.common.dto.FullSessionServerDto
import com.mypasswordgen.common.dto.SessionIDBDto
import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.mapper.SessionMapper
import com.mypasswordgen.server.model.Session
import com.mypasswordgen.server.plugins.DataNotFoundException
import com.mypasswordgen.server.repository.SessionRepository
import com.mypasswordgen.server.repository.UserRepository
import com.mypasswordgen.server.service.SessionService
import com.mypasswordgen.server.service.UserService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger("SessionServiceImpl")

class SessionServiceImpl(
    private val userService: UserService,
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
    private val sessionMapper: SessionMapper
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
        with(sessionMapper) {
            newSession.toSessionDto()
        }
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

    override fun moveAllUsers(fromSession: Session, toSession: Session) = transaction {
        logger.debug { "moveAllUsers" }
        userRepository.moveAll(fromSession.id.value, toSession.id.value)
    }

    override fun getFullSession(sessionDto: SessionDto): FullSessionClientDto = transaction {
        val session = find(sessionDto) ?: throw DataNotFoundException()
        with(sessionMapper) {
            session.toFullSessionClientDto()
        }
    }

    override fun createFullSession(sessionDto: SessionDto, fullSession: FullSessionServerDto): Pair<SessionDto, SessionIDBDto> =
        transaction {
            val newSession = sessionRepository.create()
            delete(sessionDto)
            val sessionIDBDto = SessionIDBDto {
                for (fullUser in fullSession.users) {
                    +userService.createFullUser(fullUser, newSession.id.value)
                }
            }
            val newSessionDto = with(sessionMapper) {
                newSession.toSessionDto()
            }
            Pair(newSessionDto, sessionIDBDto)
        }


}
