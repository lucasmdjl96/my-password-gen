package com.lucasmdjl.passwordgenerator.server.service

import com.lucasmdjl.passwordgenerator.server.dto.SessionDto
import com.lucasmdjl.passwordgenerator.server.model.Session
import com.lucasmdjl.passwordgenerator.server.model.User
import java.util.*

interface SessionService {

    fun assignNew(oldSessionDto: SessionDto?): Session

    fun find(sessionDto: SessionDto): Session?

    fun delete(sessionDto: SessionDto): Unit?

    fun setLastUser(sessionId: UUID, user: User?)

    fun getLastUser(sessionId: UUID): User?

    fun moveAllUsers(fromSession: Session, toSession: Session)

}
