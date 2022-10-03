package com.lucasmdjl.passwordgenerator.server.repository

import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.User

interface EmailRepository {

    fun createAndGetId(emailAddress: String, user: User): Int?

    fun getById(id: Int): Email?

    fun getByAddressAndUser(emailAddress: String, user: User): Email?

    fun delete(emailAddress: String, user: User): Unit?

}