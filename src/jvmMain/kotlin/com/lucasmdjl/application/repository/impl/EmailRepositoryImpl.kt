package com.lucasmdjl.application.repository.impl

import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.model.User
import com.lucasmdjl.application.repository.EmailRepository
import com.lucasmdjl.application.tables.Emails

object EmailRepositoryImpl : EmailRepository {

    override fun create(address: String, user: User): Email =
            Email.new {
                this.emailAddress = address
                this.user = user
            }

    override fun getByAddress(address: String): Email? =
            Email.find {
                Emails.emailAddress eq address
            }.firstOrNull()

    override fun getAllFromUser(user: User): Iterable<Email> =
            Email.find {
                Emails.user eq user.id
            }

    override fun getByAddressAndUser(address: String, user: User): Email? =
            getByAddress(address)?.takeIf { it.user.id == user.id }

}