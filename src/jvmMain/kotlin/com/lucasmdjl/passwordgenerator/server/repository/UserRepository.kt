package com.lucasmdjl.passwordgenerator.server.repository

import com.lucasmdjl.passwordgenerator.server.model.Session
import com.lucasmdjl.passwordgenerator.server.model.User
import java.util.*

interface UserRepository {

    fun createAndGetId(username: String, sessionId: UUID): Int?

    fun getById(id: Int): User?

    fun getByNameAndSession(username: String, sessionId: UUID): User?

    fun moveAll(fromSession: Session, toSession: Session)

}