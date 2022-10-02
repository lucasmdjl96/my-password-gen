package com.lucasmdjl.application.repository.impl

import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.model.Site
import com.lucasmdjl.application.repository.SiteRepository
import com.lucasmdjl.application.tables.Sites
import mu.KotlinLogging
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertIgnoreAndGetId

private val logger = KotlinLogging.logger("SiteRepositoryImpl")

object SiteRepositoryImpl : SiteRepository {

    override fun createAndGetId(siteName: String, email: Email): Int? {
        logger.debug { "createAndGetId call with siteName: $siteName, email: $email" }
        return Sites.insertIgnoreAndGetId {
            it[this.siteName] = siteName
            it[this.emailId] = email.id
        }?.value
    }

    override fun getById(id: Int): Site? {
        logger.debug { "getById call with id: $id" }
        return Site.findById(id)
    }


    override fun getByNameAndEmail(siteName: String, email: Email): Site? {
        logger.debug { "getByNameAndEmail call with siteName: $siteName, email: $email" }
        return Site.find {
            Sites.siteName eq siteName and (Sites.emailId eq email.id)
        }.firstOrNull()
    }

    override fun delete(siteName: String, email: Email): Unit? {
        logger.debug { "delete call with siteName: $siteName, email: $email" }
        return getByNameAndEmail(siteName, email)?.delete()
    }


}