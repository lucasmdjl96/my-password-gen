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

import com.mypasswordgen.common.dto.client.SiteClientDto
import com.mypasswordgen.common.dto.fullClient.FullSiteClientDto
import com.mypasswordgen.server.mapper.SiteMapper
import com.mypasswordgen.server.model.Site
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger("SiteMapperImpl")

class SiteMapperImpl : SiteMapper {

    override fun siteToSiteClientDto(site: Site): SiteClientDto {
        logger.debug { "siteToSiteClientDto" }
        return SiteClientDto(site.id.value.toString())
    }

    override fun siteToFullSiteClientDto(site: Site) = transaction {
        logger.debug { "siteToFullSiteClientDto" }
        FullSiteClientDto(site.id.value.toString())
    }

}
