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

import com.mypasswordgen.common.dto.fullServer.FullEmailServerDto
import com.mypasswordgen.common.dto.idb.EmailIDBDto
import com.mypasswordgen.common.dto.server.EmailServerDto
import com.mypasswordgen.server.mapper.EmailMapper
import com.mypasswordgen.server.plugins.DataConflictException
import com.mypasswordgen.server.plugins.DataNotFoundException
import com.mypasswordgen.server.plugins.NotEnoughInformationException
import com.mypasswordgen.server.repository.EmailRepository
import com.mypasswordgen.server.repository.SessionRepository
import com.mypasswordgen.server.repository.UserRepository
import com.mypasswordgen.server.service.EmailService
import com.mypasswordgen.server.service.SiteService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

private val logger = KotlinLogging.logger("EmailServiceImpl")

class EmailServiceImpl(
    private val siteService: SiteService,
    private val emailRepository: EmailRepository,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val emailMapper: EmailMapper
) : EmailService {

    override fun create(emailServerDto: EmailServerDto, sessionId: UUID) = transaction {
        logger.debug { "create" }
        val (emailAddress) = emailServerDto
        val user = sessionRepository.getLastUser(sessionId) ?: throw NotEnoughInformationException("No last user found.")
        if (emailRepository.getByAddressAndUser(emailAddress, user) != null) throw DataConflictException("Email address already exists.")
        val id = emailRepository.createAndGetId(emailAddress, user)
        val email = emailRepository.getById(id)!!
        userRepository.setLastEmail(user, email)
        with(emailMapper) {
            email.toEmailClientDto()
        }
    }

    override fun find(emailServerDto: EmailServerDto, sessionId: UUID) = transaction {
        logger.debug { "find" }
        val (emailAddress) = emailServerDto
        val user = sessionRepository.getLastUser(sessionId) ?: throw NotEnoughInformationException("No last user found.")
        val email = emailRepository.getByAddressAndUser(emailAddress, user) ?: throw DataNotFoundException("No such email address found.")
        userRepository.setLastEmail(user, email)
        with(emailMapper) {
            email.toEmailClientDto()
        }
    }

    override fun delete(emailServerDto: EmailServerDto, sessionId: UUID) = transaction {
        logger.debug { "delete" }
        val (emailAddress) = emailServerDto
        val user = sessionRepository.getLastUser(sessionId) ?: throw NotEnoughInformationException("No last user found.")
        val email = emailRepository.getByAddressAndUser(emailAddress, user) ?: throw DataNotFoundException("No such email address found.")
        val emailClientDto = with(emailMapper) {
            email.toEmailClientDto()
        }
        emailRepository.delete(email)
        emailClientDto
    }

    override fun createFullEmail(fullEmail: FullEmailServerDto, userId: UUID) = transaction {
        logger.debug { "createFullEmail" }
        val emailAddress = fullEmail.emailAddress
        if (emailRepository.getByAddressAndUser(emailAddress, userId) != null) throw DataConflictException("Import failed. Email address already exists.")
        val id = emailRepository.createAndGetId(emailAddress, userId)
        EmailIDBDto(emailAddress = fullEmail.emailAddress, id = id.toString()) {
            for (fullSite in fullEmail.sites) {
                +siteService.createFullSite(fullSite, id)
            }
        }
    }
}
