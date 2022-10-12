package com.lucasmdjl.passwordgenerator.server.repository.impl

import com.lucasmdjl.passwordgenerator.server.model.Session
import com.lucasmdjl.passwordgenerator.server.model.User
import com.lucasmdjl.passwordgenerator.server.repository.SessionRepository
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger("SessionRepositoryImpl")

class SessionRepositoryImpl : SessionRepository {

    override fun create(): Session {
        logger.debug { "create" }
        return Session.new {}
    }

    override fun getById(sessionId: UUID): Session? {
        logger.debug { "getById" }
        return Session.findById(sessionId)
    }

    override fun delete(session: Session) {
        logger.debug { "delete" }
        session.delete()
    }

    override fun setLastUser(session: Session, user: User?) {
        session.lastUser = user
    }

    override fun getLastUser(session: Session): User? {
        return session.lastUser
    }

}
