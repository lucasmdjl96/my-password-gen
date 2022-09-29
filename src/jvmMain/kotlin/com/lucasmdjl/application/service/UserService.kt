package com.lucasmdjl.application.service

import com.lucasmdjl.application.model.Session
import com.lucasmdjl.application.model.User
import java.util.*

interface UserService {

    fun create(username: String, sessionId: UUID): User?

    fun getByName(username: String, sessionId: UUID): User?

    fun moveAllUsers(fromSession: Session, toSession: Session)

}