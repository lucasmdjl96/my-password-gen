package com.mypasswordgen.server.service

import com.mypasswordgen.server.dto.SessionDto
import com.mypasswordgen.server.model.Session
import com.mypasswordgen.server.model.User
import java.util.*

interface SessionService {

    fun assignNew(oldSessionDto: SessionDto?): Session

    fun find(sessionDto: SessionDto): Session?

    fun delete(sessionDto: SessionDto): Unit?

    fun setLastUser(sessionId: UUID, user: User?)

    fun getLastUser(sessionId: UUID): User?

    fun moveAllUsers(fromSession: Session, toSession: Session)

}
