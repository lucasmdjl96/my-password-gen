package com.lucasmdjl.application.repository.impl

import com.lucasmdjl.application.model.Session
import com.lucasmdjl.application.repository.SessionRepository
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger("SessionRepositoryImpl")

object SessionRepositoryImpl : SessionRepository {

    override fun create(): Session {
        logger.debug { "create call with no arguments" }
        return Session.new {}
    }

    override fun getById(sessionId: UUID): Session? {
        logger.debug { "getById call with sessionID: $sessionId" }
        return Session.findById(sessionId)
    }

    override fun delete(session: Session) {
        logger.debug { "delete call with session: $session" }
        session.delete()
    }


}