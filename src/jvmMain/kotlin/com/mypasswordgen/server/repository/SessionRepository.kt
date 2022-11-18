package com.mypasswordgen.server.repository

import com.mypasswordgen.server.model.Session
import com.mypasswordgen.server.model.User
import java.util.*

interface SessionRepository {

    fun create(): Session

    fun getById(sessionId: UUID): Session?

    fun delete(session: Session)

    fun setLastUser(session: Session, user: User?)

    fun getLastUser(session: Session): User?

    fun getLastUser(sessionId: UUID): User?

    fun setLastUser(sessionId: UUID, user: User?)

}
