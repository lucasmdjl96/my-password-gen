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

import com.mypasswordgen.common.dto.client.EmailClientDto
import com.mypasswordgen.common.dto.fullClient.FullEmailClientDto
import com.mypasswordgen.common.dto.fullServer.FullEmailServerDto
import com.mypasswordgen.common.dto.idb.EmailIDBDto
import com.mypasswordgen.common.dto.server.EmailServerDto
import com.mypasswordgen.server.model.Email
import com.mypasswordgen.server.repository.crypto.encode
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.arbitrary.uuid
import java.util.*

data class FullEmailDto(val id: UUID, val emailAddress: String, val siteSet: Set<FullSiteDto>) {
    fun insertStatement(userId: UUID) = """
        |INSERT INTO EMAILS (ID, EMAIL_ADDRESS, USER_ID)
        |    VALUES ('$id', '${emailAddress.encode()}', '$userId');
        |
    """.trimMargin("|") + siteSet.joinToString("") { it.insertStatement(id) }

    fun toFullEmailClientDto() = FullEmailClientDto(id.toString()) {
        for (site in siteSet) {
            +site.toFullSiteClientDto()
        }
    }
    fun toFullEmailServerDto() = FullEmailServerDto(emailAddress) {
        for (site in siteSet) +site.toFullSiteServerDto()
    }
    fun toEmailClientDto() = EmailClientDto(id.toString(), siteSet.map { it.id.toString() }.toSet())
    fun toEmailServerDto() = EmailServerDto(emailAddress)

    fun toEmailIDBDto() = EmailIDBDto(id.toString(), emailAddress) {
        for (site in siteSet) +site.toSiteIDBDto()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FullEmailDto) return false
        if (emailAddress != other.emailAddress) return false
        return true
    }

    override fun hashCode(): Int {
        return emailAddress.hashCode()
    }

    fun encode() = FullEmailDto(
        id,
        emailAddress.encode(),
        siteSet.map { it.encode() }.toSet()
    )

    companion object {
        fun recoverFromDatabase(id: UUID): FullEmailDto? {
            val databaseEmail = Email.findById(id) ?: return null
            val sites = databaseEmail.sites.map { FullSiteDto.recoverFromDatabase(it.id.value)!! }
            return FullEmailDto(id, databaseEmail.emailAddress, sites.toSet())
        }
        fun arb(range: IntRange) = Arb.bind(
            Arb.uuid(allowNilValue = false),
            Arb.stringPattern("[^%]++"),
            Arb.set(FullSiteDto.arb(), range)
        ) { emailId, emailAddress, siteSet ->
            FullEmailDto(emailId, emailAddress, siteSet)
        }
    }
}
