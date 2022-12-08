/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.kotest.dto

import com.mypasswordgen.common.dto.client.UserClientDto
import com.mypasswordgen.common.dto.fullClient.FullUserClientDto
import com.mypasswordgen.common.dto.fullServer.FullUserServerDto
import com.mypasswordgen.common.dto.idb.UserIDBDto
import com.mypasswordgen.common.dto.server.UserServerDto
import com.mypasswordgen.server.model.User
import com.mypasswordgen.server.repository.crypto.encode
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.arbitrary.uuid
import java.util.*

data class FullUserDto(val id: UUID, val username: String, val emailSet: Set<FullEmailDto>) {
    fun insertStatement(sessionId: UUID) = """
        |INSERT INTO USERS (ID, USERNAME, SESSION_ID)
        |    VALUES ('$id', '${username.encode()}', '$sessionId');
        |
    """.trimMargin("|") + emailSet.joinToString("") { it.insertStatement(id) }

    fun makeLastEmailStatement(email: FullEmailDto) = """
        |UPDATE USERS SET LAST_EMAIL_ID='${email.id}'
        |   WHERE ID='$id';
    """.trimMargin("|")

    fun toFullUserClientDto() = FullUserClientDto(id.toString()) {
        for (email in emailSet) {
            +email.toFullEmailClientDto()
        }
    }
    fun toFullUserServerDto() = FullUserServerDto(username) {
        for (email in emailSet) +email.toFullEmailServerDto()
    }
    fun toUserClientDto() = UserClientDto(id.toString(), emailSet.map { it.id.toString() }.toSet())
    fun toUserServerDto() = UserServerDto(username)
    fun toUserIDBDto() = UserIDBDto(id.toString(), username) {
        for (email in emailSet) +email.toEmailIDBDto()
    }

    fun encode() = FullUserDto(
        id,
        username.encode(),
        emailSet.map { it.encode() }.toSet()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FullUserDto) return false
        if (username != other.username) return false
        return true
    }

    override fun hashCode(): Int {
        return username.hashCode()
    }

    companion object {
        fun recoverFromDatabase(id: UUID): FullUserDto? {
            val databaseUser = User.findById(id) ?: return null
            val emails = databaseUser.emails.map { FullEmailDto.recoverFromDatabase(it.id.value)!! }
            return FullUserDto(id, databaseUser.username, emails.toSet())
        }

        fun arb(range: IntRange) = Arb.bind(
            Arb.uuid(allowNilValue = false),
            Arb.stringPattern("[^%]++"),
            Arb.set(FullEmailDto.arb(range), range)
        ) { emailId, emailAddress, emailSet ->
            FullUserDto(emailId, emailAddress, emailSet)
        }

        fun arb(emailRange: IntRange, siteRange: IntRange) = Arb.bind(
            Arb.uuid(allowNilValue = false),
            Arb.stringPattern("[^%]++"),
            Arb.set(FullEmailDto.arb(siteRange), emailRange)
        ) { emailId, emailAddress, emailSet ->
            FullUserDto(emailId, emailAddress, emailSet)
        }
    }
}
