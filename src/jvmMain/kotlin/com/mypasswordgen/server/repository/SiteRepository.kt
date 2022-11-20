package com.mypasswordgen.server.repository

import com.mypasswordgen.server.model.Email
import com.mypasswordgen.server.model.Site
import java.util.*

interface SiteRepository {

    fun createAndGetId(siteName: String, emailId: UUID): UUID
    fun createAndGetId(siteName: String, email: Email) = createAndGetId(siteName, email.id.value)

    fun getById(id: UUID): Site?

    fun getByNameAndEmail(siteName: String, emailId: UUID): Site?
    fun getByNameAndEmail(siteName: String, email: Email) = getByNameAndEmail(siteName, email.id.value)

    fun delete(site: Site)

}
