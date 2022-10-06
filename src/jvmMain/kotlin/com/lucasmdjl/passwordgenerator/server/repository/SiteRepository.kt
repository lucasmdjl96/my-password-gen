package com.lucasmdjl.passwordgenerator.server.repository

import com.lucasmdjl.passwordgenerator.server.model.Email
import com.lucasmdjl.passwordgenerator.server.model.Site

interface SiteRepository {

    fun createAndGetId(siteName: String, email: Email): Int?

    fun getById(id: Int): Site?

    fun getByNameAndEmail(siteName: String, email: Email): Site?

    fun delete(site: Site)

}
