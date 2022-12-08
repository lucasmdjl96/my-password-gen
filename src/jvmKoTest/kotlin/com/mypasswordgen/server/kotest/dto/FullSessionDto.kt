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

import com.mypasswordgen.common.dto.fullClient.FullSessionClientDto
import com.mypasswordgen.common.dto.fullServer.FullSessionServerDto
import com.mypasswordgen.common.dto.idb.SessionIDBDto
import com.mypasswordgen.server.model.Session
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.uuid
import java.util.*

data class FullSessionDto(val id: UUID, val userSet: Set<FullUserDto>) {
    fun insertStatement() = """
        |INSERT INTO SESSIONS (ID)
        |    VALUES ('$id');
        |
    """.trimMargin("|") + userSet.joinToString("") { it.insertStatement(id) }

    fun makeLastUserStatement(user: FullUserDto) = """
        |UPDATE SESSIONS SET LAST_USER_ID='${user.id}'
        |   WHERE ID='$id';
    """.trimMargin("|")

    fun toFullSessionClientDto() = FullSessionClientDto {
        for (user in userSet) {
            +user.toFullUserClientDto()
        }
    }
    fun toFullSessionServerDto() = FullSessionServerDto {
        for (user in userSet) +user.toFullUserServerDto()
    }
    fun toSessionIDBDto() = SessionIDBDto {
        for (user in userSet) +user.toUserIDBDto()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FullSessionDto) return false
        if (this.id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun encode() = FullSessionDto(
        id,
        userSet.map { it.encode() }.toSet()
        )

    companion object {
        fun recoverFromDatabase(id: UUID): FullSessionDto? {
            val databaseSession = Session.findById(id) ?: return null
            val users = databaseSession.users.map { FullUserDto.recoverFromDatabase(it.id.value)!! }
            return FullSessionDto(id, users.toSet())
        }
        fun arb(range: IntRange) =
            Arb.bind(
                Arb.uuid(allowNilValue = false),
                Arb.set(FullUserDto.arb(range), range)
            ) { sessionId, userSet ->
                FullSessionDto(sessionId, userSet)
            }

        fun arb(userRange: IntRange, emailRange: IntRange, siteRange: IntRange) = Arb.bind(
            Arb.uuid(allowNilValue = false),
            Arb.set(FullUserDto.arb(emailRange, siteRange), userRange)
        ) { sessionId, userSet ->
            FullSessionDto(sessionId, userSet)
        }
    }
}
