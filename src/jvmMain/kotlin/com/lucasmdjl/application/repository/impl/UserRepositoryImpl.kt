package com.lucasmdjl.application.repository.impl

import com.lucasmdjl.application.model.Session
import com.lucasmdjl.application.model.User
import com.lucasmdjl.application.repository.UserRepository
import com.lucasmdjl.application.tables.Users
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update

object UserRepositoryImpl : UserRepository {

    override fun create(username: String, session: Session): User =
        User.new {
            this.session = session
            this.username = username
        }

    override fun getByNameAndSession(username: String, session: Session): User? =
        User.find {
            Users.session eq session.id and (Users.username eq username)
        }.firstOrNull()

    override fun moveAll(fromSession: Session, toSession: Session) {
        Users.update({ Users.session eq fromSession.id }) {
            it[session] = toSession.id
        }
    }


}