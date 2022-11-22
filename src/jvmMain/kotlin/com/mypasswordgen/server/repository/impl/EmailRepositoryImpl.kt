/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.repository.impl

import com.mypasswordgen.server.model.Email
import com.mypasswordgen.server.repository.EmailRepository
import com.mypasswordgen.server.repository.crypto.encode
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
            it[this.emailAddress] = emailAddress.encode()
            it[this.userId] = userId
        }.value
    }

    override fun getById(id: UUID): Email? {
        logger.debug { "getById" }
        return Email.findById(id)
    }

    override fun getByAddressAndUser(emailAddress: String, userId: UUID): Email? {
        logger.debug { "getByAddressAndUser" }
        return Email.find {
            Emails.emailAddress eq emailAddress.encode() and (Emails.userId eq userId)
        }.firstOrNull()
    }

    override fun delete(email: Email) {
        logger.debug { "delete" }
        return email.delete()
    }

}
