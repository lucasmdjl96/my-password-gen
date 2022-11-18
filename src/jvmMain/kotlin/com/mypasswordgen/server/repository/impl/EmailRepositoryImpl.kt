package com.mypasswordgen.server.repository.impl

import com.mypasswordgen.server.model.Email
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.repository.EmailRepository
import com.mypasswordgen.server.tables.Emails
import mu.KotlinLogging
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import java.util.*

private val logger = KotlinLogging.logger("EmailRepositoryImpl")

class EmailRepositoryImpl : EmailRepository {

    override fun createAndGetId(emailAddress: String, userId: UUID): UUID {
        logger.debug { "createAndGetId" }
        return Emails.insertAndGetId {
            it[this.emailAddress] = emailAddress
            it[this.userId] = userId
        }.value
    }

    override fun getById(id: UUID): Email? {
        logger.debug { "getById" }
        return Email.findById(id)
    }

    override fun getByAddressAndUser(emailAddress: String, user: User): Email? {
        logger.debug { "getByAddressAndUser" }
        return Email.find {
            Emails.emailAddress eq emailAddress and (Emails.userId eq user.id)
        }.firstOrNull()
    }

    override fun delete(email: Email) {
        logger.debug { "delete" }
        return email.delete()
    }

}
