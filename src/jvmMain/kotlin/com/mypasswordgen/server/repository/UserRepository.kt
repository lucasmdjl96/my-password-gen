package com.mypasswordgen.server.repository

import com.mypasswordgen.server.model.Email
import com.mypasswordgen.server.model.Session
import com.mypasswordgen.server.model.User
import java.util.*

interface UserRepository {

    fun createAndGetId(username: String, sessionId: UUID): UUID
    fun createAndGetId(username: String, session: Session) = createAndGetId(username, session.id.value)

    fun getById(id: UUID): User?

    fun getByNameAndSession(username: String, sessionId: UUID): User?

    fun moveAll(fromSessionId: UUID, toSessionId: UUID)

    fun setLastEmail(user: User, email: Email?)

    fun getLastEmail(user: User): Email?

}
