package com.lucasmdjl.passwordgenerator.server.repository.impl

import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.Site
import com.lucasmdjl.passwordgenerator.server.repository.SiteRepository
import com.lucasmdjl.passwordgenerator.server.tables.Sites
import mu.KotlinLogging
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertIgnoreAndGetId

private val logger = KotlinLogging.logger("SiteRepositoryImpl")

class SiteRepositoryImpl : SiteRepository {

    override fun createAndGetId(siteName: String, email: Email): Int? {
        logger.debug { "createAndGetId" }
        return Sites.insertIgnoreAndGetId {
            it[this.siteName] = siteName
            it[this.emailId] = email.id
        }?.value
    }

    override fun getById(id: Int): Site? {
        logger.debug { "getById" }
        return Site.findById(id)
    }


    override fun getByNameAndEmail(siteName: String, email: Email): Site? {
        logger.debug { "getByNameAndEmail" }
        return Site.find {
            Sites.siteName eq siteName and (Sites.emailId eq email.id)
        }.firstOrNull()
    }

    override fun delete(site: Site) {
        logger.debug { "delete" }
        return site.delete()
    }


}
