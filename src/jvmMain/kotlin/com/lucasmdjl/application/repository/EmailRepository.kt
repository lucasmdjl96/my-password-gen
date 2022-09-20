package com.lucasmdjl.application.repository

import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.model.User

interface EmailRepository {

    fun create(address: String, user: User): Email

    fun getByAddress(address: String): Email?

    fun getAllFromUser(user: User): Iterable<Email>

    fun getByAddressAndUser(address: String, user: User): Email?

}