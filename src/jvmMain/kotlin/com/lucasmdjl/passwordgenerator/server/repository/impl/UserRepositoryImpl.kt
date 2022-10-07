package com.lucasmdjl.passwordgenerator.server.repository.impl

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
        logger.debug { "createAndGetId" }
        return Users.insertIgnoreAndGetId {
            it[this.username] = username
            it[this.sessionId] = sessionId
        }?.value
    }

    override fun getById(id: Int): User? {
        logger.debug { "getById" }
        return User.findById(id)
    }

    override fun getByNameAndSession(username: String, sessionId: UUID): User? {
        logger.debug { "getByNameAndSession" }
        return User.find {
            Users.sessionId eq sessionId and (Users.username eq username)
        }.firstOrNull()
    }

    override fun moveAll(fromSessionId: UUID, toSessionId: UUID) {
        logger.debug { "moveAll" }
        Users.update({ Users.sessionId eq fromSessionId }) {
            it[sessionId] = toSessionId
        }
    }


}
