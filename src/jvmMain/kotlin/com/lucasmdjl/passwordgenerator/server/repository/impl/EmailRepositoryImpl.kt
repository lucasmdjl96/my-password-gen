package com.lucasmdjl.passwordgenerator.server.repository.impl

import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.User
import com.lucasmdjl.passwordgenerator.server.repository.EmailRepository
import com.lucasmdjl.passwordgenerator.server.tables.Emails
import mu.KotlinLogging
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertIgnoreAndGetId

private val logger = KotlinLogging.logger("EmailRepositoryImpl")

object EmailRepositoryImpl : EmailRepository {

    override fun createAndGetId(emailAddress: String, user: User): Int? {
        logger.debug { "createAndGetId call with emailAddress: $emailAddress, user: $user" }
        return Emails.insertIgnoreAndGetId {
            it[this.emailAddress] = emailAddress
            it[this.userId] = user.id
        }?.value
    }

    override fun getById(id: Int): Email? {
        logger.debug { "getById call with id: $id" }
        return Email.findById(id)
    }

    override fun getByAddressAndUser(emailAddress: String, user: User): Email? {
        logger.debug { "getByAddressAndUser call with emailAddress: $emailAddress, user: $user" }
        return Email.find {
            Emails.emailAddress eq emailAddress and (Emails.userId eq user.id)
        }.firstOrNull()
    }

    override fun delete(emailAddress: String, user: User): Unit? {
        logger.debug { "delete call with emailAddress: $emailAddress, user: $user" }
        return getByAddressAndUser(emailAddress, user)?.delete()
    }

}