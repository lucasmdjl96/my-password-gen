package com.lucasmdjl.passwordgenerator.server.service.impl

import com.lucasmdjl.passwordgenerator.common.dto.server.SiteServerDto
import com.lucasmdjl.passwordgenerator.server.emailService
import com.lucasmdjl.passwordgenerator.server.repository.SiteRepository
import com.lucasmdjl.passwordgenerator.server.repository.impl.SiteRepositoryImpl
import com.lucasmdjl.passwordgenerator.server.service.SiteService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

private val logger = KotlinLogging.logger("SiteServiceImpl")

object SiteServiceImpl : SiteService {

    private val siteRepository: SiteRepository = SiteRepositoryImpl

    override fun create(siteServerDto: SiteServerDto, sessionId: UUID) = transaction {
        logger.debug { "create" }
        val (siteName, emailServerDto) = siteServerDto
        val email = emailService.find(emailServerDto, sessionId)!!
        val id = siteRepository.createAndGetId(siteName, email)
        if (id != null) siteRepository.getById(id) else null
    }

    override fun find(siteServerDto: SiteServerDto, sessionId: UUID) = transaction {
        logger.debug { "find" }
        val (siteName, emailServerDto) = siteServerDto
        val email = emailService.find(emailServerDto, sessionId)!!
        siteRepository.getByNameAndEmail(siteName, email)
    }

    override fun delete(siteServerDto: SiteServerDto, sessionId: UUID) = transaction {
        logger.debug { "delete" }
        val site = find(siteServerDto, sessionId)
        if (site != null) siteRepository.delete(site)
        else null
    }

}
