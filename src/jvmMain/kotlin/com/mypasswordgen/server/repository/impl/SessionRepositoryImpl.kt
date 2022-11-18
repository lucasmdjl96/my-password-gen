package com.mypasswordgen.server.repository.impl

import com.mypasswordgen.server.model.Session
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.repository.SessionRepository
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

    override fun getLastUser(sessionId: UUID): User? {
        return getById(sessionId)?.lastUser
    }

    override fun setLastUser(sessionId: UUID, user: User?) {
        getById(sessionId)?.lastUser = user
    }

}
