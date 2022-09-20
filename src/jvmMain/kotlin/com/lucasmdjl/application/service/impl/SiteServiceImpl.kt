package com.lucasmdjl.application.service.impl

import com.lucasmdjl.application.mapper.EmailMapper
import com.lucasmdjl.application.mapper.SiteMapper
import com.lucasmdjl.application.mapper.impl.EmailMapperImpl
import com.lucasmdjl.application.mapper.impl.SiteMapperImpl
import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.repository.EmailRepository
import com.lucasmdjl.application.repository.SiteRepository
import com.lucasmdjl.application.repository.impl.EmailRepositoryImpl
import com.lucasmdjl.application.repository.impl.SiteRepositoryImpl
import com.lucasmdjl.application.service.SiteService
import dto.EmailDto
import dto.SiteDto
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.transactions.transaction

object SiteServiceImpl : SiteService {

    private val emailRepository: EmailRepository = EmailRepositoryImpl

    private val siteRepository: SiteRepository = SiteRepositoryImpl

    private val emailMapper: EmailMapper = EmailMapperImpl

    private val siteMapper: SiteMapper = SiteMapperImpl

    override fun addSiteToEmail(siteName: String, emailAddress: String): EmailDto =
        transaction {
            val email = emailRepository.getByAddress(emailAddress)!!
            siteRepository.create(siteName, email)
            email.load(Email::sites)
            emailMapper.emailToEmailDto(email)
        }

    override fun getSiteFromEmail(siteName: String, emailAddress: String): SiteDto? =
        transaction {
            val email = emailRepository.getByAddress(emailAddress)!!
            val site = siteRepository.getByNameAndEmail(siteName, email)
            if (site != null) siteMapper.siteToSiteDto(site) else null
        }

}