package com.lucasmdjl.passwordgenerator.server.service.impl

import com.lucasmdjl.passwordgenerator.server.emailService
import com.lucasmdjl.passwordgenerator.server.service.SiteService
import com.lucasmdjl.passwordgenerator.server.siteRepository
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object SiteServiceImpl : SiteService {

    override fun create(siteName: String, emailAddress: String, username: String, sessionId: UUID) = transaction {
        val email = emailService.find(emailAddress, username, sessionId)!!
        val id = siteRepository.createAndGetId(siteName, email)
        if (id != null) siteRepository.getById(id) else null
    }

    override fun find(siteName: String, emailAddress: String, username: String, sessionId: UUID) = transaction {
        val email = emailService.find(emailAddress, username, sessionId)!!
        siteRepository.getByNameAndEmail(siteName, email)
    }

    override fun delete(siteName: String, emailAddress: String, username: String, sessionId: UUID) = transaction {
        val email = emailService.find(emailAddress, username, sessionId)!!
        siteRepository.delete(siteName, email)
    }

}
