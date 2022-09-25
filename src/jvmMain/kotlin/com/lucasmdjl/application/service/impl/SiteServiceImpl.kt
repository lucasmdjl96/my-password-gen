package com.lucasmdjl.application.service.impl

import com.lucasmdjl.application.mapper.EmailMapper
import com.lucasmdjl.application.mapper.SiteMapper
import com.lucasmdjl.application.mapper.impl.EmailMapperImpl
import com.lucasmdjl.application.mapper.impl.SiteMapperImpl
import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.model.Session
import com.lucasmdjl.application.model.User
import com.lucasmdjl.application.repository.SessionRepository
import com.lucasmdjl.application.repository.SiteRepository
import com.lucasmdjl.application.repository.impl.SessionRepositoryImpl
import com.lucasmdjl.application.repository.impl.SiteRepositoryImpl
import com.lucasmdjl.application.service.SiteService
import dto.EmailDto
import dto.SiteDto
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object SiteServiceImpl : SiteService {

    private val sessionRepository: SessionRepository = SessionRepositoryImpl

    private val siteRepository: SiteRepository = SiteRepositoryImpl

    private val emailMapper: EmailMapper = EmailMapperImpl

    private val siteMapper: SiteMapper = SiteMapperImpl

    override fun addSiteToEmail(siteName: String, emailAddress: String, username: String, sessionId: UUID): EmailDto =
        transaction {
            val email =
                sessionRepository
                    .getById(sessionId)!!
                    .load(Session::users)
                    .users
                    .find { it.username == username }!!
                    .load(User::emails)
                    .emails
                    .find { it.emailAddress == emailAddress }!!
            siteRepository.create(siteName, email)
            email.load(Email::sites)
            emailMapper.emailToEmailDto(email)
        }

    override fun getSiteFromEmail(siteName: String, emailAddress: String, username: String, sessionId: UUID): SiteDto? =
        transaction {
            val email =
                sessionRepository
                    .getById(sessionId)!!
                    .load(Session::users)
                    .users
                    .find { it.username == username }!!
                    .load(User::emails)
                    .emails
                    .find { it.emailAddress == emailAddress }!!
            val site = siteRepository.getByNameAndEmail(siteName, email)
            if (site != null) siteMapper.siteToSiteDto(site) else null
        }

    override fun removeSiteFromEmail(
        siteName: String,
        emailAddress: String,
        username: String,
        sessionId: UUID
    ): EmailDto =
        transaction {
            val email =
                sessionRepository
                    .getById(sessionId)!!
                    .load(Session::users)
                    .users
                    .find { it.username == username }!!
                    .load(User::emails)
                    .emails
                    .find { it.emailAddress == emailAddress }!!
            siteRepository.delete(siteName, email)
            email.load(Email::sites)
            emailMapper.emailToEmailDto(email)
        }


}