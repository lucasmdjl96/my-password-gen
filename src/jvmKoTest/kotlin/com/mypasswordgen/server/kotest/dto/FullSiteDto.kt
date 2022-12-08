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

import com.mypasswordgen.common.dto.client.SiteClientDto
import com.mypasswordgen.common.dto.fullClient.FullSiteClientDto
import com.mypasswordgen.common.dto.fullServer.FullSiteServerDto
import com.mypasswordgen.common.dto.idb.SiteIDBDto
import com.mypasswordgen.common.dto.server.SiteServerDto
import com.mypasswordgen.server.model.Site
import com.mypasswordgen.server.repository.crypto.encode
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.stringPattern
import io.kotest.property.arbitrary.uuid
import java.util.*

data class FullSiteDto(val id: UUID, val siteName: String) {
    fun insertStatement(emailId: UUID) = """
        |INSERT INTO SITES (ID, SITE_NAME, EMAIL_ID)
        |    VALUES ('$id', '${siteName.encode()}', '$emailId');
        |
    """.trimMargin("|")

    fun toFullSiteClientDto() = FullSiteClientDto(id.toString())
    fun toFullSiteServerDto() = FullSiteServerDto(siteName)
    fun toSiteClientDto() = SiteClientDto(id.toString())

    fun toSiteServerDto() = SiteServerDto(siteName)

    fun toSiteIDBDto() = SiteIDBDto(id.toString(), siteName)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FullSiteDto) return false
        if (siteName != other.siteName) return false
        return true
    }

    override fun hashCode(): Int {
        return siteName.hashCode()
    }

    fun encode() = FullSiteDto(id, siteName.encode())

    companion object {
        fun recoverFromDatabase(id: UUID): FullSiteDto? {
            val databaseSite = Site.findById(id) ?: return null
            return FullSiteDto(id, databaseSite.name)
        }

        fun arb() = Arb.bind(
            Arb.uuid(allowNilValue = false),
            Arb.stringPattern("[^%]++")
        ) { siteId, siteName ->
            FullSiteDto(siteId, siteName)
        }
    }
}
