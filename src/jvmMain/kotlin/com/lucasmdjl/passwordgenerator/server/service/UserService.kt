package com.lucasmdjl.passwordgenerator.server.service

import com.lucasmdjl.passwordgenerator.server.model.Session
import com.lucasmdjl.passwordgenerator.server.model.User
import java.util.*

interface UserService {

    fun create(username: String, sessionId: UUID): User?

    fun find(username: String, sessionId: UUID): User?

    fun moveAllUsers(fromSession: Session, toSession: Session)

}
