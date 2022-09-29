package com.lucasmdjl.application.repository.impl

import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.model.User
import com.lucasmdjl.application.repository.EmailRepository
import com.lucasmdjl.application.tables.Emails
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertIgnoreAndGetId

object EmailRepositoryImpl : EmailRepository {

    override fun createAndGetId(emailAddress: String, user: User) =
        Emails.insertIgnoreAndGetId {
            it[this.emailAddress] = emailAddress
            it[this.user] = user.id
        }?.value

    override fun getById(id: Int) =
        Email.findById(id)

    override fun getAllFromUser(user: User): Iterable<Email> =
        Email.find {
            Emails.user eq user.id
        }

    override fun getByAddressAndUser(emailAddress: String, user: User) =
        Email.find {
            Emails.emailAddress eq emailAddress and (Emails.user eq user.id)
        }.firstOrNull()

    override fun delete(emailAddress: String, user: User) =
        getByAddressAndUser(emailAddress, user)?.delete()

}