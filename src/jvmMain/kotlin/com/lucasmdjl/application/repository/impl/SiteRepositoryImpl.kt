package com.lucasmdjl.application.repository.impl

import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.model.Site
import com.lucasmdjl.application.repository.SiteRepository
import com.lucasmdjl.application.tables.Sites
import org.jetbrains.exposed.sql.and

object SiteRepositoryImpl : SiteRepository {

    override fun create(siteName: String, email: Email): Site =
        Site.new {
            this.name = siteName
            this.email = email
        }

    override fun getAllFromEmail(email: Email): Iterable<Site> =
        email.sites

    override fun getByNameAndEmail(siteName: String, email: Email): Site? =
        Site.find {
            Sites.name eq siteName and (Sites.email eq email.id)
        }.firstOrNull()

    override fun delete(siteName: String, email: Email): Unit? =
        getByNameAndEmail(siteName, email)?.delete()


}