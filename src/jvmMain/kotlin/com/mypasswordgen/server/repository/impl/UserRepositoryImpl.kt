package com.mypasswordgen.server.repository.impl

import com.mypasswordgen.server.model.Email
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.repository.UserRepository
import com.mypasswordgen.server.tables.Users
import mu.KotlinLogging
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import java.util.*

private val logger = KotlinLogging.logger("UserRepositoryImpl")

class UserRepositoryImpl : UserRepository {

    override fun createAndGetId(username: String, sessionId: UUID): UUID {
        logger.debug { "createAndGetId" }
        return Users.insertAndGetId {
            it[this.username] = username
            it[this.sessionId] = sessionId
        }.value
    }

    override fun getById(id: UUID): User? {
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

    override fun setLastEmail(user: User, email: Email?) {
        logger.debug { "setLastEmail" }
        user.lastEmail = email
    }

    override fun getLastEmail(user: User): Email? {
        logger.debug { "getLastEmail" }
        return user.lastEmail
    }


}
