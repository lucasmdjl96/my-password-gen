package com.lucasmdjl.application.service.impl

import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.repository.SiteRepository
import com.lucasmdjl.application.repository.impl.SiteRepositoryImpl
import com.lucasmdjl.application.service.SiteService
import org.jetbrains.exposed.sql.transactions.transaction

object SiteServiceImpl : SiteService {

    private val siteRepository: SiteRepository = SiteRepositoryImpl

    override fun addSiteToEmail(siteName: String, email: Email) = transaction {
        val id = siteRepository.createAndGetId(siteName, email)
        if (id != null) siteRepository.getById(id) else null
    }

    override fun getSiteFromEmail(siteName: String, email: Email) = transaction {
        siteRepository.getByNameAndEmail(siteName, email)
    }

    override fun removeSiteFromEmail(siteName: String, email: Email) = transaction {
        siteRepository.delete(siteName, email)
    }


}