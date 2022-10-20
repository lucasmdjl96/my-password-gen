package com.lucasmdjl.passwordgenerator.server.repository

import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.Site
import java.util.*

interface SiteRepository {

    fun createAndGetId(siteName: String, email: Email): UUID

    fun getById(id: UUID): Site?

    fun getByNameAndEmail(siteName: String, email: Email): Site?

    fun delete(site: Site)

}
