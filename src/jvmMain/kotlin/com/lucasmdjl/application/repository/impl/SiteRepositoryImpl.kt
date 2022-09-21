package com.lucasmdjl.application.repository.impl

import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.model.Site
import com.lucasmdjl.application.repository.SiteRepository
import org.jetbrains.exposed.dao.load

object SiteRepositoryImpl : SiteRepository {

    override fun create(siteName: String, email: Email): Site =
            Site.new {
                this.name = siteName
                this.email = email
            }

    override fun getAllForEmail(email: Email): Iterable<Site> =
            email.load(Email::sites).sites

    override fun getByNameAndEmail(siteName: String, email: Email): Site? =
            getAllForEmail(email).firstOrNull { it.name == siteName }


}