package com.lucasmdjl.passwordgenerator.server.service.impl

import com.lucasmdjl.passwordgenerator.common.dto.server.SiteServerDto
import com.lucasmdjl.passwordgenerator.server.plugins.DataConflictException
import com.lucasmdjl.passwordgenerator.server.plugins.DataNotFoundException
import com.lucasmdjl.passwordgenerator.server.plugins.NotEnoughInformationException
import com.lucasmdjl.passwordgenerator.server.repository.SiteRepository
import com.lucasmdjl.passwordgenerator.server.repository.UserRepository
import com.lucasmdjl.passwordgenerator.server.service.SessionService
import com.lucasmdjl.passwordgenerator.server.service.SiteService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

private val logger = KotlinLogging.logger("SiteServiceImpl")

class SiteServiceImpl(
    private val siteRepository: SiteRepository,
    private val userRepository: UserRepository,
    private val sessionService: SessionService
) : SiteService {

    override fun create(siteServerDto: SiteServerDto, sessionId: UUID) = transaction {
        logger.debug { "create" }
        val (siteName) = siteServerDto
        val user = sessionService.getLastUser(sessionId) ?: throw NotEnoughInformationException()
        val email = userRepository.getLastEmail(user) ?: throw NotEnoughInformationException()
        val id = siteRepository.createAndGetId(siteName, email) ?: throw DataConflictException()
        siteRepository.getById(id) ?: throw DataNotFoundException()
    }

    override fun find(siteServerDto: SiteServerDto, sessionId: UUID) = transaction {
        logger.debug { "find" }
        val (siteName) = siteServerDto
        val user = sessionService.getLastUser(sessionId) ?: throw NotEnoughInformationException()
        val email = userRepository.getLastEmail(user) ?: throw NotEnoughInformationException()
        siteRepository.getByNameAndEmail(siteName, email) ?: throw DataNotFoundException()
    }

    override fun delete(siteServerDto: SiteServerDto, sessionId: UUID) = transaction {
        logger.debug { "delete" }
        val site = find(siteServerDto, sessionId)
        siteRepository.delete(site)
    }

}
