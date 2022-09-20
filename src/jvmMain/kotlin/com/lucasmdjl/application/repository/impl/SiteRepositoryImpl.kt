package com.lucasmdjl.application.repository.impl

import com.lucasmdjl.application.model.Email
import com.lucasmdjl.application.model.Site
import com.lucasmdjl.application.repository.SiteRepository
import com.lucasmdjl.application.tables.Sites
import org.jetbrains.exposed.dao.load

object SiteRepositoryImpl : SiteRepository {

    override fun create(name: String, email: Email): Site =
            Site.new {
                this.name = name
                this.email = email
            }

    override fun getByName(name: String): Site? =
            Site.find {
                Sites.name eq name
            }.firstOrNull()

    override fun getAllForEmail(email: Email): Iterable<Site> =
            email.load(Email::sites).sites

    override fun getByNameAndEmail(name: String, email: Email): Site? =
            getAllForEmail(email).firstOrNull { it.name == name }


}