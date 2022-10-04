package com.lucasmdjl.passwordgenerator.server.repository.impl

import com.lucasmdjl.passwordgenerator.server.model.Session
import com.lucasmdjl.passwordgenerator.server.model.User
import com.lucasmdjl.passwordgenerator.server.repository.UserRepository
import com.lucasmdjl.passwordgenerator.server.tables.Users
import mu.KotlinLogging
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertIgnoreAndGetId
import org.jetbrains.exposed.sql.update
import java.util.*

private val logger = KotlinLogging.logger("UserRepositoryImpl")

object UserRepositoryImpl : UserRepository {

    override fun createAndGetId(username: String, sessionId: UUID): Int? {
        logger.debug { "createAndGetId call with username: $username, sessionId: $sessionId" }
        return Users.insertIgnoreAndGetId {
            it[this.username] = username
            it[this.sessionId] = sessionId
        }?.value
    }

    override fun getById(id: Int): User? {
        logger.debug { "getById call with id: $id" }
        return User.findById(id)
    }

    override fun getByNameAndSession(username: String, sessionId: UUID): User? {
        logger.debug { "getByNameAndSession call with username: $username, sessionId: $sessionId" }
        return User.find {
            Users.sessionId eq sessionId and (Users.username eq username)
        }.firstOrNull()
    }

    override fun moveAll(fromSession: Session, toSession: Session) {
        logger.debug { "moveAll call with fromSession: $fromSession, toSession: $toSession" }
        Users.update({ Users.sessionId eq fromSession.id }) {
            it[sessionId] = toSession.id
        }
    }


}