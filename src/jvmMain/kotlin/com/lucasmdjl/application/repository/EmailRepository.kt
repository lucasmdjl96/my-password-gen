package com.lucasmdjl.application.repository

import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.model.User

interface EmailRepository {

    fun create(emailAddress: String, user: User): Int?

    fun getById(id: Int): Email?

    fun getAllFromUser(user: User): Iterable<Email>

    fun getByAddressAndUser(emailAddress: String, user: User): Email?

    fun delete(emailAddress: String, user: User): Unit?

}