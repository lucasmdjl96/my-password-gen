package com.mypasswordgen.server.repository.impl

import com.mypasswordgen.server.model.Email
import com.mypasswordgen.server.model.Site
import com.mypasswordgen.server.repository.SiteRepository
import com.mypasswordgen.server.tables.Sites
import mu.KotlinLogging
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import java.util.*

private val logger = KotlinLogging.logger("SiteRepositoryImpl")

class SiteRepositoryImpl : SiteRepository {

    override fun createAndGetId(siteName: String, email: Email): UUID {
        logger.debug { "createAndGetId" }
        return Sites.insertAndGetId {
            it[this.siteName] = siteName
            it[this.emailId] = email.id
        }.value
    }

    override fun getById(id: UUID): Site? {
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