package com.lucasmdjl.application.repository.impl

import com.lucasmdjl.application.model.User
import com.lucasmdjl.application.repository.UserRepository
import com.lucasmdjl.application.tables.Users
import org.jetbrains.exposed.sql.transactions.transaction

object UserRepositoryImpl : UserRepository {

    override fun create(userName: String): User =
            User.new {
                this.username = userName
            }

    override fun getByName(userName: String): User? =
            User.find {
                Users.username eq userName
            }.firstOrNull()



}