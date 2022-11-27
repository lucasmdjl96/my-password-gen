/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.mapper.impl

import com.mypasswordgen.common.dto.client.EmailClientDto
import com.mypasswordgen.common.dto.fullClient.FullEmailClientDto
import com.mypasswordgen.server.mapper.EmailMapper
import com.mypasswordgen.server.mapper.SiteMapper
import com.mypasswordgen.server.model.Email
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger("EmailMapperImpl")

class EmailMapperImpl(private val siteMapper: SiteMapper) : EmailMapper {

    override fun emailToEmailClientDto(email: Email): EmailClientDto = transaction {
        logger.debug { "emailToEmailClientDto" }
        EmailClientDto(
            email.id.value.toString(),
            email.sites.map { site -> site.id.value.toString() }.toSet()
        )
    }

    override fun emailToFullEmailClientDto(email: Email) = transaction {
        logger.debug { "emailToFullEmailClientDto" }
        FullEmailClientDto(email.id.value.toString()) {
            email.sites.forEach { site ->
                with(siteMapper) {
                    +site.toFullSiteClientDto()
                }
            }
        }
    }

}
