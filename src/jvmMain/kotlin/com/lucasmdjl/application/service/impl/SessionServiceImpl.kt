package com.lucasmdjl.application.service.impl

import com.lucasmdjl.application.model.Session
import com.lucasmdjl.application.repository.SessionRepository
import com.lucasmdjl.application.repository.impl.SessionRepositoryImpl
import com.lucasmdjl.application.service.SessionService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

private val logger = KotlinLogging.logger("SessionServiceImpl")

object SessionServiceImpl : SessionService {

    private val sessionRepository: SessionRepository = SessionRepositoryImpl

    override fun create(): Session = transaction {
        logger.debug { "create call with no arguments" }
        sessionRepository.create()
    }

    override fun getById(sessionId: UUID): Session? = transaction {
        logger.debug { "getById call with sessionId: $sessionId" }
        sessionRepository.getById(sessionId)
    }

    override fun delete(session: Session) = transaction {
        logger.debug { "delete call with session: $session" }
        sessionRepository.delete(session)
    }

}