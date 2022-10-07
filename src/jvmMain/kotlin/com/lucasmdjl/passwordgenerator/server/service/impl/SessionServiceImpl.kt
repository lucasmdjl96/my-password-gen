package com.lucasmdjl.passwordgenerator.server.service.impl

import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.repository.SessionRepository
import com.lucasmdjl.passwordgenerator.server.repository.impl.SessionRepositoryImpl
import com.lucasmdjl.passwordgenerator.server.service.SessionService
import com.lucasmdjl.passwordgenerator.server.userService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger("SessionServiceImpl")

object SessionServiceImpl : SessionService {

    private val sessionRepository: SessionRepository = SessionRepositoryImpl

    override fun assignNew(oldSessionDto: SessionDto?) = transaction {
        logger.debug { "assignNew" }
        val newSession = sessionRepository.create()
        if (oldSessionDto != null) {
            val oldSession = find(oldSessionDto)
            if (oldSession != null) {
                userService.moveAllUsers(oldSession, newSession)
                sessionRepository.delete(oldSession)
            }
        }
        newSession
    }

    override fun find(sessionDto: SessionDto) = transaction {
        logger.debug { "find" }
        sessionRepository.getById(sessionDto.sessionId)
    }

    override fun delete(sessionDto: SessionDto): Unit = transaction {
        logger.debug { "delete" }
        val session = find(sessionDto)
        if (session != null) sessionRepository.delete(session)
    }

}
