package com.mypasswordgen.server.repository

import com.mypasswordgen.server.model.Email
import com.mypasswordgen.server.model.User
import java.util.*

interface EmailRepository {

    fun createAndGetId(emailAddress: String, userId: UUID): UUID
    fun createAndGetId(emailAddress: String, user: User) = createAndGetId(emailAddress, user.id.value)

    fun getById(id: UUID): Email?

    fun getByAddressAndUser(emailAddress: String, user: User): Email?

    fun delete(email: Email)

}
