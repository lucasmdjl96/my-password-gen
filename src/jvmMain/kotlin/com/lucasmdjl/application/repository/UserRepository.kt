package com.lucasmdjl.application.repository

import com.lucasmdjl.application.model.Session
import com.lucasmdjl.application.model.User
import java.util.*

interface UserRepository {

    fun create(username: String, sessionId: UUID): Int?

    fun getById(id: Int): User?

    fun getByNameAndSession(username: String, sessionId: UUID): User?

    fun moveAll(fromSession: Session, toSession: Session)

}