package com.lucasmdjl.passwordgenerator.server.service.impl

import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.repository.SiteRepository
import com.lucasmdjl.passwordgenerator.server.repository.impl.SiteRepositoryImpl
import com.lucasmdjl.passwordgenerator.server.service.SiteService
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger("SiteServiceImpl")

object SiteServiceImpl : SiteService {

    private val siteRepository: SiteRepository = SiteRepositoryImpl

    override fun addSiteToEmail(siteName: String, email: Email) = transaction {
        logger.debug { "addSiteToEmail call with siteName: $siteName, email: $email" }
        val id = siteRepository.createAndGetId(siteName, email)
        if (id != null) siteRepository.getById(id) else null
    }

    override fun getSiteFromEmail(siteName: String, email: Email) = transaction {
        logger.debug { "getSiteFromEmail call with siteName: $siteName, email: $email" }
        siteRepository.getByNameAndEmail(siteName, email)
    }

    override fun removeSiteFromEmail(siteName: String, email: Email) = transaction {
        logger.debug { "removeSiteFromEmail call with siteName: $siteName, email: $email" }
        siteRepository.delete(siteName, email)
    }


}