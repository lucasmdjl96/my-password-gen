package com.lucasmdjl.passwordgenerator.server.repository

import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.User
import java.util.*

interface UserRepository {

    fun createAndGetId(username: String, sessionId: UUID): Int?

    fun getById(id: Int): User?

    fun getByNameAndSession(username: String, sessionId: UUID): User?

    fun moveAll(fromSessionId: UUID, toSessionId: UUID)

    fun setLastEmail(user: User, email: Email?)

    fun getLastEmail(user: User): Email?

}
