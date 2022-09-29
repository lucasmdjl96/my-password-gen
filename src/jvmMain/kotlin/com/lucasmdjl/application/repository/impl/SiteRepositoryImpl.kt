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

    override fun createAndGetId(siteName: String, email: Email) =
        Sites.insertIgnoreAndGetId {
            it[this.name] = siteName
            it[this.email] = email.id
        }?.value

    override fun getById(id: Int) =
        Site.findById(id)

    override fun getAllFromEmail(email: Email): Iterable<Site> =
        email.sites

    override fun getByNameAndEmail(siteName: String, email: Email) =
        Site.find {
            Sites.name eq siteName and (Sites.email eq email.id)
        }.firstOrNull()

    override fun delete(siteName: String, email: Email) =
        getByNameAndEmail(siteName, email)?.delete()


}