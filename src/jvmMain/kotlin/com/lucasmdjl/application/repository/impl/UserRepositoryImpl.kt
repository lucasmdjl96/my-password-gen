package com.lucasmdjl.application.repository.impl

import com.lucasmdjl.application.model.Session
import com.lucasmdjl.application.model.User
import com.lucasmdjl.application.repository.UserRepository
import com.lucasmdjl.application.tables.Users
import org.jetbrains.exposed.sql.and

object UserRepositoryImpl : UserRepository {

    override fun create(username: String, session: Session): User =
            User.new {
                this.session = session
                this.username = username
            }

    override fun getByName(username: String, session: Session): User? =
            User.find {
                Users.session eq session.id and (Users.username eq username)
            }.firstOrNull()



}