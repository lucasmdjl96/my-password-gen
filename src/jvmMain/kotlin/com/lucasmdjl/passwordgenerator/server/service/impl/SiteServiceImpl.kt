package com.lucasmdjl.passwordgenerator.server.service.impl

import com.lucasmdjl.passwordgenerator.common.dto.server.SiteServerDto
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
        val user = sessionService.getLastUser(sessionId)!!
        val email = userRepository.getLastEmail(user)!!
        val id = siteRepository.createAndGetId(siteName, email)
        if (id != null) siteRepository.getById(id) else null
    }

    override fun find(siteServerDto: SiteServerDto, sessionId: UUID) = transaction {
        logger.debug { "find" }
        val (siteName) = siteServerDto
        val user = sessionService.getLastUser(sessionId)!!
        val email = userRepository.getLastEmail(user)!!
        siteRepository.getByNameAndEmail(siteName, email)
    }

    override fun delete(siteServerDto: SiteServerDto, sessionId: UUID) = transaction {
        logger.debug { "delete" }
        val site = find(siteServerDto, sessionId)
        if (site != null) siteRepository.delete(site)
        else null
    }

}
