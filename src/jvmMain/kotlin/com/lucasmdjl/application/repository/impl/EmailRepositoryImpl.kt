package com.lucasmdjl.application.repository.impl

import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.model.User
import com.lucasmdjl.application.repository.EmailRepository
import com.lucasmdjl.application.tables.Emails
import org.jetbrains.exposed.sql.and

object EmailRepositoryImpl : EmailRepository {

    override fun create(emailAddress: String, user: User): Email =
        Email.new {
            this.emailAddress = emailAddress
            this.user = user
        }


    override fun getAllFromUser(user: User): Iterable<Email> =
        Email.find {
            Emails.user eq user.id
        }

    override fun getByAddressAndUser(emailAddress: String, user: User): Email? =
        Email.find {
            Emails.emailAddress eq emailAddress and (Emails.user eq user.id)
        }.firstOrNull()

    override fun delete(emailAddress: String, user: User) =
        getByAddressAndUser(emailAddress, user)?.delete()

}