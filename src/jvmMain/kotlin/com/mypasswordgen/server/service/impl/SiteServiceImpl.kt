package com.mypasswordgen.server.service.impl

import com.mypasswordgen.common.dto.server.SiteServerDto
import com.mypasswordgen.server.mapper.SiteMapper
import com.mypasswordgen.server.plugins.DataConflictException
import com.mypasswordgen.server.plugins.DataNotFoundException
import com.mypasswordgen.server.plugins.NotEnoughInformationException
import com.mypasswordgen.server.repository.SiteRepository
import com.mypasswordgen.server.repository.UserRepository
import com.mypasswordgen.server.service.SessionService
import com.mypasswordgen.server.service.SiteService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

private val logger = KotlinLogging.logger("SiteServiceImpl")

class SiteServiceImpl(
    private val siteRepository: SiteRepository,
    private val userRepository: UserRepository,
    private val sessionService: SessionService,
    private val siteMapper: SiteMapper
) : SiteService {

    override fun create(siteServerDto: SiteServerDto, sessionId: UUID) = transaction {
        logger.debug { "create" }
        val (siteName) = siteServerDto
        val user = sessionService.getLastUser(sessionId) ?: throw NotEnoughInformationException()
        val email = userRepository.getLastEmail(user) ?: throw NotEnoughInformationException()
        if (siteRepository.getByNameAndEmail(siteName, email) != null) throw DataConflictException()
        val id = siteRepository.createAndGetId(siteName, email)
        val site = siteRepository.getById(id) ?: throw DataNotFoundException()
        with(siteMapper) {
            site.toSiteClientDto()
        }
    }

    override fun find(siteServerDto: SiteServerDto, sessionId: UUID) = transaction {
        logger.debug { "find" }
        val (siteName) = siteServerDto
        val user = sessionService.getLastUser(sessionId) ?: throw NotEnoughInformationException()
        val email = userRepository.getLastEmail(user) ?: throw NotEnoughInformationException()
        val site = siteRepository.getByNameAndEmail(siteName, email) ?: throw DataNotFoundException()
        with(siteMapper) {
            site.toSiteClientDto()
        }
    }

    override fun delete(siteServerDto: SiteServerDto, sessionId: UUID) = transaction {
        logger.debug { "delete" }
        val (siteName) = siteServerDto
        val user = sessionService.getLastUser(sessionId) ?: throw NotEnoughInformationException()
        val email = userRepository.getLastEmail(user) ?: throw NotEnoughInformationException()
        val site = siteRepository.getByNameAndEmail(siteName, email) ?: throw DataNotFoundException()
        val siteClientDto = with(siteMapper) {
            site.toSiteClientDto()
        }
        siteRepository.delete(site)
        siteClientDto
    }

}