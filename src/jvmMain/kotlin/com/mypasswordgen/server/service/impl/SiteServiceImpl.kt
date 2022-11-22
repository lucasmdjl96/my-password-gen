/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.service.impl

import com.mypasswordgen.common.dto.fullServer.FullSiteServerDto
import com.mypasswordgen.common.dto.idb.SiteIDBDto
import com.mypasswordgen.common.dto.server.SiteServerDto
import com.mypasswordgen.server.mapper.SiteMapper
import com.mypasswordgen.server.plugins.DataConflictException
import com.mypasswordgen.server.plugins.DataNotFoundException
import com.mypasswordgen.server.plugins.NotEnoughInformationException
import com.mypasswordgen.server.repository.SessionRepository
import com.mypasswordgen.server.repository.SiteRepository
import com.mypasswordgen.server.repository.UserRepository
import com.mypasswordgen.server.service.SiteService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

private val logger = KotlinLogging.logger("SiteServiceImpl")

class SiteServiceImpl(
    private val siteRepository: SiteRepository,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val siteMapper: SiteMapper
) : SiteService {

    override fun create(siteServerDto: SiteServerDto, sessionId: UUID) = transaction {
        logger.debug { "create" }
        val (siteName) = siteServerDto
        val user = sessionRepository.getLastUser(sessionId) ?: throw NotEnoughInformationException("No last user found.")
        val email = userRepository.getLastEmail(user) ?: throw NotEnoughInformationException("No last email found.")
        if (siteRepository.getByNameAndEmail(siteName, email) != null) throw DataConflictException("Site name already exists.")
        val id = siteRepository.createAndGetId(siteName, email)
        val site = siteRepository.getById(id)!!
        with(siteMapper) {
            site.toSiteClientDto()
        }
    }

    override fun find(siteServerDto: SiteServerDto, sessionId: UUID) = transaction {
        logger.debug { "find" }
        val (siteName) = siteServerDto
        val user = sessionRepository.getLastUser(sessionId) ?: throw NotEnoughInformationException("No last user found.")
        val email = userRepository.getLastEmail(user) ?: throw NotEnoughInformationException("No last email found.")
        val site = siteRepository.getByNameAndEmail(siteName, email) ?: throw DataNotFoundException("Site name not found.")
        with(siteMapper) {
            site.toSiteClientDto()
        }
    }

    override fun delete(siteServerDto: SiteServerDto, sessionId: UUID) = transaction {
        logger.debug { "delete" }
        val (siteName) = siteServerDto
        val user = sessionRepository.getLastUser(sessionId) ?: throw NotEnoughInformationException("No last user found.")
        val email = userRepository.getLastEmail(user) ?: throw NotEnoughInformationException("No last email found.")
        val site = siteRepository.getByNameAndEmail(siteName, email) ?: throw DataNotFoundException("Site name not found.")
        val siteClientDto = with(siteMapper) {
            site.toSiteClientDto()
        }
        siteRepository.delete(site)
        siteClientDto
    }

    override fun createFullSite(fullSite: FullSiteServerDto, emailId: UUID) = transaction {
        logger.debug { "createFullSite" }
        val siteName = fullSite.siteName
        if (siteRepository.getByNameAndEmail(siteName, emailId) != null) throw DataConflictException("Import failed. Site name already exists.")
        val id = siteRepository.createAndGetId(siteName, emailId)
        SiteIDBDto(siteName = fullSite.siteName, id = id.toString())
    }

}
